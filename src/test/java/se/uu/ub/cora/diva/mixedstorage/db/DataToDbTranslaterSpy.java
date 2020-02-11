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

import se.uu.ub.cora.data.DataGroup;

public class DataToDbTranslaterSpy implements DataToDbTranslater {

	public DataGroup dataGroup;
	public Map<String, Object> conditions;
	public Map<String, Object> values;

	@Override
	public void translate(DataGroup dataGroup) {
		this.dataGroup = dataGroup;
		DataGroup recordInfo = dataGroup.getFirstGroupWithNameInData("recordInfo");
		String organistaionId = recordInfo.getFirstAtomicValueWithNameInData("id");

		conditions = new HashMap<>();
		conditions.put("organisation_id", Integer.parseInt(organistaionId));
		conditions.put("someConditionKeyFromSpy", "someConditionValueFromSpy");
		values = new HashMap<>();
		values.put("someValuesKeyFromSpy", "someValuesValueFromSpy");

	}

	@Override
	public Map<String, Object> getConditions() {
		return conditions;
	}

	@Override
	public Map<String, Object> getValues() {
		return values;
	}

}
