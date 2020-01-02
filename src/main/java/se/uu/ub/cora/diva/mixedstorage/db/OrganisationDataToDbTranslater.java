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

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;

public class OrganisationDataToDbTranslater implements DataToDbTranslater {

	private Map<String, Object> values = new HashMap<>();
	private Map<String, Object> conditions = new HashMap<>(1);
	private DataGroup dataGroup;

	@Override
	public void translate(DataGroup dataGroup) {
		this.dataGroup = dataGroup;
		values = new HashMap<>();
		conditions = new HashMap<>(1);

		createConditionsAddingOrganisationId();
		createColumnsWithValuesForUpdateQuery();
	}

	private Map<String, Object> createConditionsAddingOrganisationId() {
		String id = extractIdFromDataGroup();
		throwDbExceptionIfIdNotAnIntegerValue(id);
		conditions.put("organisation_id", Integer.valueOf(id));
		return conditions;
	}

	private String extractIdFromDataGroup() {
		DataGroup recordInfo = dataGroup.getFirstGroupWithNameInData("recordInfo");
		return recordInfo.getFirstAtomicValueWithNameInData("id");
	}

	private void throwDbExceptionIfIdNotAnIntegerValue(String id) {
		try {
			Integer.valueOf(id);
		} catch (NumberFormatException ne) {
			throw DbException.withMessageAndException("Record not found: " + id, ne);
		}
	}

	private Map<String, Object> createColumnsWithValuesForUpdateQuery() {
		addAtomicValuesToColumns();
		addEligible();
		return values;
	}

	private void addEligible() {
		boolean notEligible = false;
		if (eligibleIsNoOrMissing()) {
			notEligible = true;
		}
		values.put("not_eligible", notEligible);
	}

	private boolean eligibleIsNoOrMissing() {
		return !dataGroup.containsChildWithNameInData("eligible")
				|| "no".equals(dataGroup.getFirstAtomicValueWithNameInData("eligible"));
	}

	private void addAtomicValuesToColumns() {
		for (OrganisationAtomicColumns column : OrganisationAtomicColumns.values()) {
			addAtomicValueOrNullToColumn(column);
		}
	}

	private void addAtomicValueOrNullToColumn(OrganisationAtomicColumns column) {
		String coraName = column.coraName;
		String dbColumnName = column.dbName;
		if (!dataGroupHasValue(coraName)) {
			values.put(dbColumnName, null);
		} else {
			handleAndAddValue(coraName, dbColumnName, column);
		}

	}

	private boolean dataGroupHasValue(String coraName) {
		return dataGroup.containsChildWithNameInData(coraName);
	}

	private void handleAndAddValue(String coraName, String dbColumnName,
			OrganisationAtomicColumns column) {
		String type = column.type;
		String value = dataGroup.getFirstAtomicValueWithNameInData(coraName);
		if ("date".equals(type)) {
			addDataValue(dbColumnName, value);
		} else {
			values.put(dbColumnName, value);
		}
	}

	private void addDataValue(String dbColumnName, String value) {
		Date valueAsDate = Date.valueOf(value);
		values.put(dbColumnName, valueAsDate);
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
