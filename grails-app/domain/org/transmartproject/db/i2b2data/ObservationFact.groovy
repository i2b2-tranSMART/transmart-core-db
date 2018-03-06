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

package org.transmartproject.db.i2b2data

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes = ['encounterNum', 'conceptCode', 'providerId', 'startDate', 'modifierCd', 'instanceNum'])
class ObservationFact implements Serializable {

	public static final String TYPE_TEXT = 'T'
	public static final String TYPE_NUMBER = 'N'

	/* links to concept_dimension, but concept_code is not primary key in
	 * concept_dimension. Hence, i2b2 1.3 added a concept_path column here */
	String conceptCode

	String valueType
	String textValue
	BigDecimal numberValue
	String valueFlag
	String sourcesystemCd

	// these are not used, but we need them because they're not nullable
	BigDecimal encounterNum
	String providerId
	Date startDate
	String modifierCd
	Long instanceNum

	static belongsTo = [patient: PatientDimension,]

	static mapping = {
		table 'I2B2DEMODATA.observation_fact'
		id composite: ['encounterNum', 'patient', 'conceptCode', 'providerId', 'startDate', 'modifierCd']
		version false

		conceptCode column: 'concept_cd'
		numberValue column: 'nval_num'
		patient column: 'patient_num'
		textValue column: 'tval_char'
		valueFlag column: 'valueflag_cd'
		valueType column: 'valtype_cd'
	}

	static constraints = {
		conceptCode maxSize: 50
		modifierCd maxSize: 100
		numberValue nullable: true, scale: 5
		patient nullable: true
		providerId maxSize: 50
		sourcesystemCd nullable: true, maxSize: 50
		textValue nullable: true
		valueFlag nullable: true, maxSize: 50
		valueType nullable: true, maxSize: 50
	}
}
