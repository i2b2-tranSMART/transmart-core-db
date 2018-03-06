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

package org.transmartproject.db.dataquery.highdim.rnaseqcog

import groovy.transform.EqualsAndHashCode
import org.transmartproject.db.dataquery.highdim.DeSubjectSampleMapping
import org.transmartproject.db.i2b2data.PatientDimension

@EqualsAndHashCode(includes = ['assay', 'annotation'])
class DeSubjectRnaData implements Serializable {

	BigDecimal logIntensity
	BigDecimal rawIntensity
	BigDecimal zscore

	DeRnaseqAnnotation jAnnotation //due to criteria bug

	static belongsTo = [
			annotation: DeRnaseqAnnotation,
			assay     : DeSubjectSampleMapping,
			patient   : PatientDimension
	]

	static mapping = {
		table schema: 'deapp'
		id composite: ['assay', 'annotation']
		version false

		annotation column: 'probeset_id' // poor name; no probes involved

		// here due to criteria bug
		jAnnotation column: 'probeset_id', insertable: false, updateable: false
	}

	static constraints = {
		annotation nullable: true
		assay nullable: true
		logIntensity nullable: true, scale: 4
		rawIntensity nullable: true, scale: 4
		zscore nullable: true, scale: 4
	}
}
