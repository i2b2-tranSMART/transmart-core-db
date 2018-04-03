/*
 * Copyright © 2013-2014 The Hyve B.V.
 *
 * This file is part of transmart-core-db.
 *
 * Transmart-core-db is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * transmart-core-db.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transmartproject.db.accesscontrol

import grails.compiler.GrailsCompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.transmartproject.core.concept.ConceptKey
import org.transmartproject.core.exceptions.UnexpectedResultException
import org.transmartproject.core.ontology.ConceptsResource
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.core.ontology.StudiesResource
import org.transmartproject.core.ontology.Study
import org.transmartproject.core.querytool.Item
import org.transmartproject.core.querytool.Panel
import org.transmartproject.core.querytool.QueryDefinition
import org.transmartproject.core.querytool.QueryResult
import org.transmartproject.core.users.ProtectedOperation
import org.transmartproject.db.ontology.I2b2Secure
import org.transmartproject.db.user.User

import static org.transmartproject.db.ontology.AbstractAcrossTrialsOntologyTerm.ACROSS_TRIALS_TABLE_CODE

/**
 * Access control methods for use in {@link org.transmartproject.db.user.User}.
 *
 * Right now these are implemented in this same class.
 * If this list gets bigger or needs to be pluggable by other plugins, then
 * this should be refactored into a different solution (e.g. each check
 * implemented in a different Spring bean).
 */
@GrailsCompileStatic
@Component
@Slf4j('logger')
class AccessControlChecks {

	public static final String PUBLIC_SOT = 'EXP:PUBLIC'

	@Autowired ConceptsResource conceptsResource
	@Autowired StudiesResource studiesResource

	boolean canPerform(User user, ProtectedOperation protectedOperation, Study study) {

		if (user.admin) {
			/* administrators bypass all the checks */
			logger.debug 'Bypassing check for {} on {} for user {} because he is an administrator',
					protectedOperation, study, this
			return true
		}

		// Get the study's "token"
		I2b2Secure secure = I2b2Secure.findWhere(fullName: study.ontologyTerm.fullName)
		if (!secure) {
			logger.warn 'Could not find object "${}" in i2b2_secure; allowing access',
					study.ontologyTerm.fullName
			// must be true for backwards compatibility reasons; see I2b2HelperService::getAccess
			return true
		}

		String token = secure.secureObjectToken
		if (!token) {
			throw new UnexpectedResultException("Found i2b2secure object with empty token")
		}
		logger.debug 'Token for {} is {}', study, token

		// if token is EXP:PUBLIC, always permit
		if (token == PUBLIC_SOT) {
			return true
		}

		/* see if the user has some access level for this
		 * soav.user will be null to indicate access to EVERYONE_GROUP */
		List<AccessLevel> results = SecuredObjectAccessView.executeQuery('''
            select soav.accessLevel
            from SecuredObjectAccessView soav
            where (soav.user = :user OR soav.user is null)
              and soav.securedObject.bioDataUniqueId = :token
              and soav.securedObject.dataType = 'BIO_CLINICAL_TRIAL' ''',
				[token: token, user: user]) as List<AccessLevel>

		logger.debug 'Got access levels for user {}, token {}: {}', this, token, results

		if (!results) {
			logger.info 'No access level entries found for user {} and token {}; denying access',
					this, token
			return false
		}

		if (results.any { protectedOperation in it }) {
			logger.debug 'Access level of user {} for token {} granted through permission {}',
					this, token, results.find { protectedOperation in it }
			true
		}
		else {
			logger.info 'Permissions of user {} for token {} are only {}; denying access',
					this, token, results as Set
			false
		}
	}

	// Study is included if the user has ANY kind of access

	Set<Study> getAccessibleStudiesForUser(User user) {
		// this method could benefit from caching
		Set<Study> studySet = studiesResource.studySet
		if (user.admin) {
			return studySet
		}

		List<I2b2Secure> allI2b2s = I2b2Secure.findAllByFullNameInList(studySet*.ontologyTerm*.fullName as List)

		allI2b2s.find { !it.secureObjectToken }?.collect {
			throw new UnexpectedResultException("Found I2b2Secure object with empty secureObjectToken")
		}

		Map<Study, String> studySOTMap = studySet.collectEntries { Study study ->
			/* note that the user is GRANTED access to studies that don't
			 * have a corresponding I2b2Secure object. That is
			 * the reason for "?.(...) ?: PUBLIC_SOT" part below
			 */
			[study, allI2b2s.find { i2b2secure ->
				i2b2secure.fullName == study.ontologyTerm.fullName
			}?.secureObjectToken ?: PUBLIC_SOT]
		}

		List<String> nonPublicSOTs = allI2b2s.findAll {
			it.secureObjectToken && it.secureObjectToken != PUBLIC_SOT
		}*.secureObjectToken

		List<String> userGrantedSOTs
		if (nonPublicSOTs) {
			userGrantedSOTs = SecuredObjectAccessView.executeQuery('''
				select soav.securedObject.bioDataUniqueId
				from SecuredObjectAccessView soav
				where (soav.user = :user OR soav.user is null)
				  and soav.securedObject.bioDataUniqueId IN (:tokens)
				  and soav.securedObject.dataType = 'BIO_CLINICAL_TRIAL' ''',
			[tokens: nonPublicSOTs, user: user]) as List<String>
		}
		else {
			userGrantedSOTs = []
		}

		studySet.findAll { Study study ->
			studySOTMap[study] == PUBLIC_SOT || studySOTMap[study] in userGrantedSOTs
		}
	}

	boolean canPerform(User user, ProtectedOperation operation, QueryDefinition definition) {
		if (operation != ProtectedOperation.WellKnownOperations.BUILD_COHORT) {
			logger.warn 'Requested protected operation different from BUILD_COHORT on QueryDefinition {}',
					definition
			throw new UnsupportedOperationException("Operation $operation ")
		}

		// check there is at least one non-inverted panel for which the user
		// has permission in all the terms
		boolean ok = definition.panels.findAll { !it.invert }.any { Panel panel ->
			Set<Study> foundStudies = panel.items.findAll { Item item ->
				/* always permit across trial nodes.
				 * Across trial terms have no study, so they have to be
				 * handled especially */
				new ConceptKey(item.conceptKey).tableCode != ACROSS_TRIALS_TABLE_CODE
			}.collect { Item item ->
				/* this could be optimized by adding a new method in
				 * StudiesResource */
				OntologyTerm concept = conceptsResource.getByKey(item.conceptKey)
				Study study = concept.study

				if (study == null) {
					logger.info 'User included concept with no study: {}', concept
				}

				study
			} as Set

			foundStudies.every { Study s ->
				s != null && canPerform(user, operation, s)
			}
		}

		if (!ok) {
			logger.warn 'User {} defined access for definition {} because it does' +
					' not include one non-inverted panel for which the user has ' +
					'permission in all the terms\' studies', user, definition
		}
		else {
			logger.debug 'Granting access to user {} to use query definition {}',
					user, definition
		}

		ok
	}

	boolean canPerform(User user, ProtectedOperation operation, QueryResult result) {
		if (operation != ProtectedOperation.WellKnownOperations.READ) {
			logger.warn 'Requested protected operation different from READ on QueryResult {}', result
			throw new UnsupportedOperationException("Operation $operation ")
		}

		/* Note that this check doesn't account for the fact that the user's
		 * permissions on the check from which the result was generated may
		 * have been revoked in the meantime. We could check again the access
		 * to the studies with:
		 *
		 * def qd = queryDefinitionXml.fromXml(new StringReader(
		 *         queryResult.queryInstance.queryMaster.requestXml))
		 * canPerform(user, BUILD_COHORT, qd)
		 *
		 * However, this would be less efficient and is not deemed necessary
		 * at this point.
		 *
		 * Another option would be to expire user's query results when his
		 * permissions change, but this can be tricky if the permissions
		 * are changed on a group or if the stuff is reimported.
		 *
		 */
		boolean ok = result.username == user.username

		if (!ok) {
			logger.warn 'Denying {} access to query result {} because its creator ({}) does not match the user ({})',
					user, result, result.username, user.username
		}
		else {
			logger.debug 'Granting {} access to {} (usernames match)', user, result
		}

		ok
	}
}
