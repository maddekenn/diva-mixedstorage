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
package se.uu.ub.cora.diva.mixedstorage.db.organisation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DataToDbHelper;
import se.uu.ub.cora.diva.mixedstorage.db.DataToDbRepeatableTranslater;

public class OrganisationParentDataToDbTranslater implements DataToDbRepeatableTranslater {
	private List<Map<String, Object>> repeatableValues = new ArrayList<>();
	private DataGroup dataGroup;

	@Override
	public void translate(DataGroup dataGroup) {
		this.dataGroup = dataGroup;
		repeatableValues = new ArrayList<>();
		addParentsValuesToList(dataGroup);
	}

	private void addParentsValuesToList(DataGroup dataGroup) {
		List<DataGroup> parents = dataGroup.getAllGroupsWithNameInData("parentOrganisation");
		for (DataGroup parentGroup : parents) {
			addParentValuesToList(parentGroup);
		}
	}

	private void addParentValuesToList(DataGroup parentGroup) {
		Map<String, Object> values = new HashMap<>();
		addIntegerIdToValues(values);
		addParentIdToValues(parentGroup, values);
		repeatableValues.add(values);
	}

	private void addIntegerIdToValues(Map<String, Object> values) {
		String id = DataToDbHelper.extractIdFromDataGroup(dataGroup);
		DataToDbHelper.throwDbExceptionIfIdNotAnIntegerValue(id);
		values.put("organisation_id", Integer.valueOf(id));
	}

	private void addParentIdToValues(DataGroup parentGroup, Map<String, Object> values) {
		DataGroup parent = parentGroup.getFirstGroupWithNameInData("organisationLink");
		String parentId = parent.getFirstAtomicValueWithNameInData("linkedRecordId");
		DataToDbHelper.throwDbExceptionIfIdNotAnIntegerValue(parentId);

		values.put("organisation_parent_id", Integer.valueOf(parentId));
	}

	@Override
	public List<Map<String, Object>> getRepeatableValues() {
		return repeatableValues;
	}
}
