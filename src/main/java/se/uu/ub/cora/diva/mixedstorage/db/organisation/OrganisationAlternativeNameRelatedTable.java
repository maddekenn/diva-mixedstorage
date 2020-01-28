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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DataToDbHelper;
import se.uu.ub.cora.diva.mixedstorage.db.DbException;
import se.uu.ub.cora.diva.mixedstorage.db.DbStatement;
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
	private Map<String, Object> alternativeNameRow;

	public OrganisationAlternativeNameRelatedTable(RecordReader recordReader,
			RecordDeleter recordDeleter, RecordCreator recordCreator,
			Map<String, Object> alternativeNameRow) {
		this.recordReader = recordReader;
		this.recordDeleter = recordDeleter;
		this.recordCreator = recordCreator;
		this.alternativeNameRow = alternativeNameRow;
	}

	@Override
	public List<DbStatement> handleDbForDataGroup(DataGroup organisation) {
		throwExceptionIfAlternativeNameIsMissing(organisation);

		String organisationId = DataToDbHelper.extractIdFromDataGroup(organisation);
		DataToDbHelper.throwDbExceptionIfIdNotAnIntegerValue(organisationId);

		List<DbStatement> dbStatements = new ArrayList<>();

		readAndHandleAlternativeName(organisation, organisationId, dbStatements);
		return dbStatements;
	}

	private void throwExceptionIfAlternativeNameIsMissing(DataGroup organisation) {
		if (!organisationContainsAlternativeName(organisation)) {
			throw DbException.withMessage("Organisation must contain alternative name");
		}
	}

	private void readAndHandleAlternativeName(DataGroup organisation, String organisationId,
			List<DbStatement> dbStatements) {
		handleAlternativeName(organisation, organisationId, alternativeNameRow, dbStatements);
	}

	private void handleAlternativeName(DataGroup organisation, String organisationId,
			Map<String, Object> readRow, List<DbStatement> dbStatements) {
		boolean organisationContainsAlternativeName = organisationContainsAlternativeName(
				organisation);

		if (alternativeNameExistsInDatabase(readRow)) {
			handleUpdate(organisation, organisationId, readRow, organisationContainsAlternativeName,
					dbStatements);
		} else {
			possiblyInsertAlternativeName(organisation, organisationId,
					organisationContainsAlternativeName, dbStatements);
		}
	}

	private void possiblyInsertAlternativeName(DataGroup organisation, String organisationId,
			boolean organisationContainsAlternativeName, List<DbStatement> dbStatements) {
		if (organisationContainsAlternativeName) {
			insertNewAlternativeName(dbStatements, organisation, organisationId);
		}
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

	private void handleUpdate(DataGroup organisation, String organisationId,
			Map<String, Object> readRow, boolean organisationContainsAlternativeName,
			List<DbStatement> dbStatements) {
		if (organisationContainsAlternativeName) {
			compareAndHandleExistingAlternativeName(organisation, readRow, organisationId,
					dbStatements);
		} else {
			deleteAlternativeName(readRow);
		}
	}

	private void compareAndHandleExistingAlternativeName(DataGroup organisation,
			Map<String, Object> readRow, String organisationId, List<DbStatement> dbStatements) {
		boolean nameInDataGroupDiffersFromNameInDb = nameInDbNotSameAsNameInDataGroup(readRow,
				organisation);
		if (nameInDataGroupDiffersFromNameInDb) {
			int nameId = (int) readRow.get(ORGANISATION_NAME_ID);
			updateAlternativeName(dbStatements, organisation, organisationId, nameId);
		}

	}

	private void updateAlternativeName(List<DbStatement> dbStatements, DataGroup organisation,
			String organisationId, int nameId) {
		Map<String, Object> values = generateValues(organisation, organisationId);
		Map<String, Object> conditions = new HashMap<>();
		conditions.put(ORGANISATION_NAME_ID, nameId);
		dbStatements.add(new DbStatement("update", ORGANISATION_NAME, values, conditions));
	}

	private Map<String, Object> generateValues(DataGroup organisation, String organisationId) {
		Map<String, Object> values = new HashMap<>();
		values.put("locale", "en");
		values.put("organisation_id", Integer.valueOf(organisationId));
		values.put("last_updated", getCurrentTimestamp());
		values.put(ORGANISATION_NAME, getAlternativeNameFromOrganisation(organisation));
		return values;
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

	private void insertNewAlternativeName(List<DbStatement> dbStatements, DataGroup organisation,
			String organisationId) {
		Map<String, Object> values = generateValues(organisation, organisationId);
		addPrimaryKeyForInsert(values);
		dbStatements
				.add(new DbStatement("insert", ORGANISATION_NAME, values, Collections.emptyMap()));
	}

	private void addPrimaryKeyForInsert(Map<String, Object> values) {
		Map<String, Object> nextValue = recordReader.readNextValueFromSequence("name_sequence");
		values.put(ORGANISATION_NAME_ID, nextValue.get("nextval"));
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
