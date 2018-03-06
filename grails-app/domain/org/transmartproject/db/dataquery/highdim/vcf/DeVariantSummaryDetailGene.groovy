package org.transmartproject.db.dataquery.highdim.vcf

import org.transmartproject.db.dataquery.highdim.DeSubjectSampleMapping

class DeVariantSummaryDetailGene implements Serializable {

	// it's a view

	// from sumary
	String chr
	Long pos
	String subjectId
	String rsId
	String variant
	String variantFormat
	String variantType
	Boolean reference
	Integer allele1
	Integer allele2

	//from detail
	String ref
	String alt
	String quality
	String filter
	String info
	String format
	String variantValue

	// from population data with gene info restriction
	String geneName
	String geneId

	DeVariantSubjectIdxCoreDb subjectIndex

	static belongsTo = [
			dataset: DeVariantDatasetCoreDb,
			assay  : DeSubjectSampleMapping
	]

	static mapping = {
		table schema: 'deapp'
		id column: 'variant_subject_summary_id'
		version false

		dataset insertable: false, updateable: false
		quality column: 'qual'
		subjectId column: 'subject_id', insertable: false, updateable: false

		subjectIndex {
			column name: 'dataset_id'
			column name: 'subject_id'
		}
	}
}
