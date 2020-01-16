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
package se.uu.ub.cora.diva.mixedstorage.db.organisation;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DataToDbHelper;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTable;
import se.uu.ub.cora.sqldatabase.RecordCreator;
import se.uu.ub.cora.sqldatabase.RecordDeleter;
import se.uu.ub.cora.sqldatabase.RecordReader;

public class OrganisationAlternativeNameRelatedTable implements RelatedTable {

	private static final String ORGANISATION_NAME_ID = "organisation_name_id";
	private static final String ORGANISATION_NAME = "organisation_name";
	private static final String ALTERNATIVE_NAME = "alternativeName";
	private RecordReader recordReader;
	private RecordDeleter recordDeleter;
	private RecordCreator recordCreator;

	public OrganisationAlternativeNameRelatedTable(RecordReader recordReader,
			RecordDeleter recordDeleter, RecordCreator recordCreator) {
		this.recordReader = recordReader;
		this.recordDeleter = recordDeleter;
		this.recordCreator = recordCreator;
	}

	@Override
	public void handleDbForDataGroup(DataGroup organisation) {
		String organisationId = DataToDbHelper.extractIdFromDataGroup(organisation);
		DataToDbHelper.throwDbExceptionIfIdNotAnIntegerValue(organisationId);
		Map<String, Object> conditions = createConditionsWithOrganisationIdAndDefaultLocale(
				organisationId);

		readAndHandleAlternativeName(organisation, organisationId, conditions);
	}

	private void readAndHandleAlternativeName(DataGroup organisation, String organisationId,
			Map<String, Object> conditions) {
		List<Map<String, Object>> readRows = recordReader
				.readFromTableUsingConditions(ORGANISATION_NAME, conditions);
		Map<String, Object> readRow = getRowIfOnlyOneOrEmptyMap(readRows);
		handleAlternativeName(organisation, organisationId, readRow);
	}

	private Map<String, Object> getRowIfOnlyOneOrEmptyMap(List<Map<String, Object>> readRows) {
		return readRows.size() == 1 ? readRows.get(0) : Collections.emptyMap();
	}

	private void handleAlternativeName(DataGroup organisation, String organisationId,
			Map<String, Object> readRow) {
		boolean organisationContainsAlternativeName = organisationContainsAlternativeName(
				organisation);

		if (alternativeNameExistsInDatabase(readRow)) {
			handleDeleteAndPossibleInsert(organisation, organisationId, readRow,
					organisationContainsAlternativeName);
		} else {
			possiblyInsertAlternativeName(organisation, organisationId,
					organisationContainsAlternativeName);
		}
	}

	private void possiblyInsertAlternativeName(DataGroup organisation, String organisationId,
			boolean organisationContainsAlternativeName) {
		if (organisationContainsAlternativeName) {
			insertNewAlternativeName(organisation, organisationId);
		}
	}

	private Map<String, Object> createConditionsWithOrganisationIdAndDefaultLocale(
			String organisationId) {
		Map<String, Object> conditions = new HashMap<>();
		conditions.put("locale", "en");
		conditions.put("organisation_id", Integer.valueOf(organisationId));
		return conditions;
	}

	private boolean organisationContainsAlternativeName(DataGroup organisation) {
		return organisation.containsChildWithNameInData(ALTERNATIVE_NAME)
				&& alternativeNameContainsValueForName(organisation);

	}

	private boolean alternativeNameContainsValueForName(DataGroup organisation) {
		DataGroup alternativeNameGroup = organisation.getFirstGroupWithNameInData(ALTERNATIVE_NAME);
		return alternativeNameGroup.containsChildWithNameInData("organisationName");
	}

	private boolean alternativeNameExistsInDatabase(Map<String, Object> readRow) {
		return !readRow.isEmpty();
	}

	private void handleDeleteAndPossibleInsert(DataGroup organisation, String organisationId,
			Map<String, Object> readRow, boolean organisationContainsAlternativeName) {
		if (organisationContainsAlternativeName) {
			compareAndHandleExistingAlternativeName(organisation, readRow, organisationId);
		} else {
			deleteAlternativeName(readRow);
		}
	}

	private void compareAndHandleExistingAlternativeName(DataGroup organisation,
			Map<String, Object> readRow, String organisationId) {
		boolean nameInDataGroupDiffersFromNameInDb = nameInDbNotSameAsNameInDataGroup(readRow,
				organisation);
		if (nameInDataGroupDiffersFromNameInDb) {
			deleteAlternativeName(readRow);
			insertNewAlternativeName(organisation, organisationId);
		}
	}

	private boolean nameInDbNotSameAsNameInDataGroup(Map<String, Object> readRow,
			DataGroup organisation) {
		String nameOfOrganisation = getAlternativeNameFromOrganisation(organisation);
		String organisationNameInDb = (String) readRow.get(ORGANISATION_NAME);
		return !nameOfOrganisation.equals(organisationNameInDb);
	}

	private void deleteAlternativeName(Map<String, Object> readRow) {
		Map<String, Object> deleteConditions = new HashMap<>();
		int nameId = (int) readRow.get(ORGANISATION_NAME_ID);
		deleteConditions.put(ORGANISATION_NAME_ID, nameId);
		recordDeleter.deleteFromTableUsingConditions(ORGANISATION_NAME, deleteConditions);
	}

	private void insertNewAlternativeName(DataGroup organisation, String organisationId) {
		Map<String, Object> values = createValuesForInsert(organisation, organisationId);
		recordCreator.insertIntoTableUsingNameAndColumnsWithValues(ORGANISATION_NAME, values);
	}

	private Map<String, Object> createValuesForInsert(DataGroup organisation,
			String organisationId) {
		Map<String, Object> values = new HashMap<>();
		Map<String, Object> nextValue = recordReader.readNextValueFromSequence("name_sequence");
		values.put(ORGANISATION_NAME_ID, nextValue.get("nextval"));
		values.put("last_updated", getCurrentTimestamp());
		values.put("locale", "en");
		values.put(ORGANISATION_NAME, getAlternativeNameFromOrganisation(organisation));
		values.put("organisation_id", Integer.valueOf(organisationId));
		return values;
	}

	private Timestamp getCurrentTimestamp() {
		Date today = new Date();
		long time = today.getTime();
		return new Timestamp(time);

	}

	private String getAlternativeNameFromOrganisation(DataGroup organisation) {
		DataGroup alternativeNameGroup = organisation.getFirstGroupWithNameInData(ALTERNATIVE_NAME);
		return alternativeNameGroup.getFirstAtomicValueWithNameInData("organisationName");
	}

	public RecordReader getRecordReader() {
		// needed for test
		return recordReader;
	}

	public RecordDeleter getRecordDeleter() {
		// needed for test
		return recordDeleter;
	}

	public RecordCreator getRecordCreator() {
		// needed for test
		return recordCreator;
	}

}
