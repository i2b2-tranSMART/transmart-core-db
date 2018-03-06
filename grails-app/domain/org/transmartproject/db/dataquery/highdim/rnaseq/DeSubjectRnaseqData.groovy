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

package org.transmartproject.db.dataquery.highdim.rnaseq

import groovy.transform.EqualsAndHashCode
import org.transmartproject.core.dataquery.highdim.rnaseq.RnaSeqValues
import org.transmartproject.db.dataquery.highdim.DeSubjectSampleMapping
import org.transmartproject.db.dataquery.highdim.chromoregion.DeChromosomalRegion
import org.transmartproject.db.i2b2data.PatientDimension

@EqualsAndHashCode(includes = ['assay', 'region'])
class DeSubjectRnaseqData implements RnaSeqValues, Serializable {

	Double logNormalizedReadcount
	Double normalizedReadcount
	Integer readcount
	String trialName
	Double zscore

	// see comment in mapping
	DeChromosomalRegion jRegion

	/* unused; should be the same as assay.patient */
	PatientDimension patient

	static belongsTo = [
			assay  : DeSubjectSampleMapping,
			patient: PatientDimension,
			region : DeChromosomalRegion
	]

	static mapping = {
		table schema: 'deapp'
		id composite: ['assay', 'region']
		version false
		sort assay: 'asc'

		// this duplicate mapping is needed due to a Criteria bug.
		// see https://forum.hibernate.org/viewtopic.php?f=1&t=1012372
		jRegion column: 'region_id', insertable: false, updateable: false
	}

	static constraints = {
		assay nullable: true
		logNormalizedReadcount nullable: true
		normalizedReadcount nullable: true
		patient nullable: true
		readcount nullable: true
		region nullable: true
		trialName nullable: true, maxSize: 50
		zscore nullable: true
	}
}
