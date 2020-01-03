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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;

public class OrganisationAlternativeNameDataToDbTranslater implements DataToDbTranslater {
	private Map<String, Object> values = new HashMap<>();
	private DataGroup dataGroup;

	@Override
	public void translate(DataGroup dataGroup) {
		this.dataGroup = dataGroup;
		values = new HashMap<>();
		addValues();
	}

	private void addValues() {
		addIntegerIdToValues();
		DataGroup alternativeName = dataGroup.getFirstGroupWithNameInData("alternativeName");
		addOrganisationNameToValues(alternativeName);
		addLocaleToValues(alternativeName);
	}

	private void addIntegerIdToValues() {
		String id = extractIdFromDataGroup();
		throwDbExceptionIfIdNotAnIntegerValue(id);
		values.put("organisation_id", Integer.valueOf(id));
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

	private void addOrganisationNameToValues(DataGroup alternativeName) {
		String organisationName = alternativeName
				.getFirstAtomicValueWithNameInData("organisationName");
		values.put("organisation_name", organisationName);
	}

	private void addLocaleToValues(DataGroup alternativeName) {
		String locale = alternativeName.getFirstAtomicValueWithNameInData("language");
		values.put("locale", locale);
	}

	@Override
	public Map<String, Object> getValues() {
		return values;
	}

	@Override
	public Map<String, Object> getConditions() {
		return Collections.emptyMap();
	}
}
