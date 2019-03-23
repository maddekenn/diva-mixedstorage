/*
 * Copyright 2019 Uppsala University Library
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

import se.uu.ub.cora.bookkeeper.data.DataGroup;

public class CoraToDbOrganisationConverter implements CoraToDbConverter {

	@Override
	public PreparedStatementInfo convert(DataGroup dataGroup) {

		Map<String, Object> values = createValues(dataGroup);
		Map<String, Object> conditions = createConditions(dataGroup);
		return PreparedStatementInfo.withTableNameValuesAndConditions("organisation", values,
				conditions);
	}

	private Map<String, Object> createValues(DataGroup dataGroup) {
		Map<String, Object> values = new HashMap<>();
		extractAndAddName(dataGroup, values);
		return values;
	}

	private void extractAndAddName(DataGroup dataGroup, Map<String, Object> values) {
		String name = dataGroup.getFirstAtomicValueWithNameInData("organisationName");
		values.put("organisation_name", name);
	}

	private Map<String, Object> createConditions(DataGroup dataGroup) {
		Map<String, Object> conditions = new HashMap<>();
		extractAndAddIdAsCondition(dataGroup, conditions);
		return conditions;
	}

	private void extractAndAddIdAsCondition(DataGroup dataGroup, Map<String, Object> conditions) {
		DataGroup recordInfo = dataGroup.getFirstGroupWithNameInData("recordInfo");
		String id = recordInfo.getFirstAtomicValueWithNameInData("id");
		conditions.put("organisation_id", Integer.valueOf(id));
	}

}
