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

import com.google.common.base.Objects
import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.Multimap
import org.transmartproject.core.users.Permission
import org.transmartproject.core.users.ProtectedOperation

import static org.transmartproject.core.users.ProtectedOperation.WellKnownOperations.API_READ
import static org.transmartproject.core.users.ProtectedOperation.WellKnownOperations.BUILD_COHORT
import static org.transmartproject.core.users.ProtectedOperation.WellKnownOperations.EXPORT
import static org.transmartproject.core.users.ProtectedOperation.WellKnownOperations.RUN_ANALYSIS
import static org.transmartproject.core.users.ProtectedOperation.WellKnownOperations.SHOW_IN_TABLE
import static org.transmartproject.core.users.ProtectedOperation.WellKnownOperations.SHOW_SUMMARY_STATISTICS

class AccessLevel implements Permission {

	String name
	Long value

	static hasMany = [searchAuthSecObjectAccesses: SecuredObjectAccess]

	static mapping = {
		table 'searchapp.search_sec_access_level'
		id column: 'search_sec_access_level_id', generator: 'assigned'
		version false

		name column: 'access_level_name'
		value column: 'access_level_value'
	}

	static constraints = {
		name nullable: true, maxSize: 200
		value nullable: true
	}

	/**
	 * This is not in the database for now.
	 *
	 * Note that {@link Permission}s are only used for Studies at this
	 * point. This mapping between permissions and operations are
	 * specific for Studies.
	 *
	 * Access to QueryDefinitions don't need to rely on QueryDefinition
	 * specific permissions because are implemented in function of
	 * Study access and access to QueryResults is implemented in function of
	 * the user that submitted the query. AccessLevels are not involved.
	 */
	@Lazy
	static Multimap<String, ProtectedOperation> permissionToOperations = {
		def mapBuilder = ImmutableMultimap.builder()

		for (operation in [API_READ, BUILD_COHORT, SHOW_SUMMARY_STATISTICS, RUN_ANALYSIS, EXPORT, SHOW_IN_TABLE]) {
			mapBuilder.put 'OWN', operation
			mapBuilder.put 'EXPORT', operation
		}

		// all but api read, export and show in table to VIEW
		for (operation in [BUILD_COHORT, SHOW_SUMMARY_STATISTICS, RUN_ANALYSIS]) {
			mapBuilder.put 'VIEW', operation
		}

		mapBuilder.build()
	}()

	String toString() {
		Objects.toStringHelper(this)
				.add("name", name)
				.add("value", value).toString()
	}

	boolean isCase(ProtectedOperation operation) {
		permissionToOperations.containsEntry(name, operation)
	}
}
