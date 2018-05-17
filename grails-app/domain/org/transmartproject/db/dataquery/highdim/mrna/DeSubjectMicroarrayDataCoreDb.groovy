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

package org.transmartproject.db.dataquery.highdim.mrna

import groovy.transform.EqualsAndHashCode
import org.transmartproject.db.dataquery.highdim.DeSubjectSampleMapping
import org.transmartproject.db.i2b2data.PatientDimension

@EqualsAndHashCode(includes = ['assay', 'probe'])
class DeSubjectMicroarrayDataCoreDb implements Serializable {

	BigDecimal logIntensity // log2(rawIntensity)
	BigDecimal rawIntensity
	String trialName
	BigDecimal zscore

	DeMrnaAnnotationCoreDb jProbe //see comment on mapping

	static belongsTo = [assay  : DeSubjectSampleMapping,
	                    patient: PatientDimension,
	                    probe  : DeMrnaAnnotationCoreDb]

	static mapping = {
		table 'deapp.de_subject_microarray_data'
		id composite: ['assay', 'probe']
		version false

		probe column: 'probeset_id'

		// this is needed due to a Criteria bug.
		// see https://forum.hibernate.org/viewtopic.php?f=1&t=1012372
		jProbe column: 'probeset_id', insertable: false, updateable: false
	}

	static constraints = {
		assay nullable: true
		logIntensity nullable: true, scale: 4
		patient nullable: true
		probe nullable: true
		rawIntensity nullable: true
		trialName nullable: true, maxSize: 50
		zscore nullable: true
	}
}
