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

package org.transmartproject.db.dataquery.highdim

import com.google.common.collect.AbstractIterator
import com.google.common.collect.Iterators
import com.google.common.collect.PeekingIterator
import groovy.transform.CompileStatic
import org.transmartproject.core.dataquery.DataColumn
import org.transmartproject.core.dataquery.TabularResult

@CompileStatic
class RepeatedEntriesCollectingTabularResult<T extends AbstractDataRow> {

	@Delegate
	TabularResult<DataColumn, T> tabularResult

	Closure collectBy = Closure.IDENTITY

	Closure<T> resultItem = { List<T> it -> (T) it[0] }

	Iterator<T> getRows() {
		new RepeatedEntriesCollectingIterator(tabularResult.iterator())
	}

	Iterator<T> iterator() {
		getRows()
	}

	// @CompileStatic
	class RepeatedEntriesCollectingIterator extends AbstractIterator<T> {

		PeekingIterator<T> sourceIterator

		RepeatedEntriesCollectingIterator(Iterator<T> sourceIterator) {
			sourceIterator = (PeekingIterator<T>) Iterators.peekingIterator((Iterator) sourceIterator)
		}

		protected T computeNext() {
			List<T> collected = []
			if (!sourceIterator.hasNext()) {
				endOfData()
				return
			}

			collected << (T) sourceIterator.next()
			while (sourceIterator.hasNext() &&
					collectBy.call(sourceIterator.peek()) != null &&
					collectBy.call(sourceIterator.peek()) == collectBy.call(collected[0])) {
				collected.add((T) sourceIterator.next())
			}

			(T) resultItem.call(collected)
		}
	}
}
