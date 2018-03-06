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

package org.transmartproject.db.dataquery.highdim.rbm

import org.transmartproject.db.dataquery.highdim.DeGplInfo

class DeRbmAnnotation {

	String antigenName
	String geneId
	String geneSymbol
	String gplId
	String uniprotId
	String uniprotName

	static belongsTo = [platform: DeGplInfo]

	static mapping = {
		table schema: 'deapp', name: 'de_rbm_annotation'
		id generator: 'assigned'
		version false

		platform column: 'gpl_id'
		gplId insertable: false, updateable: false
	}

	static constraints = {
		antigenName maxSize: 800
		geneId nullable: true, maxSize: 400
		geneSymbol nullable: true, maxSize: 200
		gplId maxSize: 50
		platform nullable: true
		uniprotId nullable: true, maxSize: 200
		uniprotName nullable: true, maxSize: 200
	}
}
