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

package org.transmartproject.db.dataquery.highdim.vcf

import org.transmartproject.db.dataquery.highdim.DeSubjectSampleMapping

class DeVariantSubjectSummaryCoreDb {

	static final Integer REF_ALLELE = 0

	Integer allele1
	Integer allele2
	String chr
	DeVariantSubjectDetailCoreDb jDetail
	Long pos
	Boolean reference
	String rsId
	String subjectId
	DeVariantSubjectIdxCoreDb subjectIndex
	String variant
	String variantFormat
	String variantType

	static belongsTo = [assay: DeSubjectSampleMapping, dataset: DeVariantDatasetCoreDb]
	//TODO: implement constraint on dataset

	static constraints = {
		subjectIndex nullable: true
		variant nullable: true
		variantFormat nullable: true
		variantType nullable: true
	}

	static mapping = {
		table 'deapp.de_variant_subject_summary'
		version false
		id column: 'variant_subject_summary_id',
				generator: 'sequence', params: [sequence: 'deapp.de_variant_subject_summary_seq']

		// this is needed due to a Criteria bug.
		// see https://forum.hibernate.org/viewtopic.php?f=1&t=1012372
		columns {
			jDetail(insertable: false, updateable: false) {
				column name: 'dataset_id'
				column name: 'rs_id'
				column name: 'chr'
				column name: 'pos'
			}
			subjectIndex(insertable: false, updateable: false) {
				column name: 'dataset_id'
				column name: 'subject_id'
			}
		}
	}
}
