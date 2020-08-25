/*
 * Copyright 2020 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.uu.ub.cora.diva.mixedstorage.db;

import java.util.HashMap;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;

public class FilterToDbConditionsConverterImp implements FilterToDbConditionsConverter {

	@Override
	public Map<String, Object> convert(DataGroup filter) {
		Map<String, Object> conditions = new HashMap<>();
		for (DataGroup part : filter.getAllGroupsWithNameInData("part")) {
			convertFilterPart(conditions, part);
		}
		return conditions;
	}

	private void convertFilterPart(Map<String, Object> conditions, DataGroup part) {
		String key = part.getFirstAtomicValueWithNameInData("key");
		String value = part.getFirstAtomicValueWithNameInData("value");
		conditions.put(key, value);
	}

}
