package org.transmartproject.db.dataquery.highdim.vcf

class DeVariantPopulationDataCoreDb {

	String chromosome
	DeVariantDatasetCoreDb dataset
	Double floatValue
	Integer infoIndex
	String infoName
	Long integerValue
	Long position
	String textValue

	static constraints = {
		floatValue nullable: true
		infoIndex nullable: true
		infoName nullable: true
		integerValue nullable: true
		textValue nullable: true
	}

	static mapping = {
		table 'deapp.de_variant_population_data'
		id column: 'variant_population_data_id', generator: 'sequence'
		version false

		chromosome column: 'chr'
		position column: 'pos'
	}
}
