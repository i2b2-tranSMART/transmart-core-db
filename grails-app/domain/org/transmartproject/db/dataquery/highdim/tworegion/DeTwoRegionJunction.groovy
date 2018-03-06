package org.transmartproject.db.dataquery.highdim.tworegion

import groovy.transform.EqualsAndHashCode
import org.transmartproject.core.dataquery.highdim.tworegion.Junction
import org.transmartproject.db.dataquery.highdim.DeSubjectSampleMapping

/**
 * @author j.hudecek
 */
@EqualsAndHashCode()
class DeTwoRegionJunction implements Serializable, Junction {

	DeSubjectSampleMapping assay
	String downChromosome
	Long downEnd
	Long downPos
	Character downStrand
	Boolean isInFrame
	String upChromosome
	Long upEnd
	Long upPos
	Character upStrand

	Boolean isInFrame() {
		isInFrame
	}

	static hasMany = [junctionEvents: DeTwoRegionJunctionEvent]

	static constraints = {
		downChromosome maxSize: 50
		downStrand nullable: true
		isInFrame nullable: true
		upChromosome maxSize: 50
		upStrand nullable: true
	}

	static mapping = {
		table 'deapp.de_two_region_junction'
		id column: 'two_region_junction_id'
		version false

		downChromosome column: 'down_chr'
		downStrand sqlType: 'char', length: 1
		upChromosome column: 'up_chr'
		upStrand sqlType: 'char', length: 1
	}
}
