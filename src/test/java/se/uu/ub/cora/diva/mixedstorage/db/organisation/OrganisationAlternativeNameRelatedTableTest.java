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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.db.DbException;
import se.uu.ub.cora.diva.mixedstorage.db.DbStatement;
import se.uu.ub.cora.diva.mixedstorage.db.RecordCreatorSpy;
import se.uu.ub.cora.diva.mixedstorage.db.RecordDeleterSpy;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTable;

public class OrganisationAlternativeNameRelatedTableTest {

	private RecordReaderRelatedTableSpy recordReader;
	private RecordDeleterSpy recordDeleter;
	private RecordCreatorSpy recordCreator;
	private RelatedTable alternativeName;

	@BeforeMethod
	public void setUp() {
		recordReader = new RecordReaderRelatedTableSpy();
		recordDeleter = new RecordDeleterSpy();
		recordCreator = new RecordCreatorSpy();
		Map<String, Object> alternativeNameRow = new HashMap<>();
		alternativeNameRow.put("organisation_name_id", 234);
		alternativeNameRow.put("organisation_id", 678);
		alternativeNameRow.put("organisation_name", "some english name");
		alternativeNameRow.put("locale", "en");
		alternativeName = new OrganisationAlternativeNameRelatedTable(recordReader, recordDeleter,
				recordCreator, alternativeNameRow);

	}

	@Test(expectedExceptions = DbException.class, expectedExceptionsMessageRegExp = ""
			+ "Organisation must contain alternative name")
	public void testNoNameInDataGroupThrowsException() {
		DataGroup organisation = createDataGroupWithId("678");
		alternativeName.handleDbForDataGroup(organisation);
	}

	private DataGroup createDataGroupWithId(String id) {
		DataGroup dataGroup = new DataGroupSpy("organisation");
		DataGroupSpy recordInfo = new DataGroupSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", id));
		dataGroup.addChild(recordInfo);
		return dataGroup;
	}

	private void addNameToReturnFromSpy(String tableName, int nameId, int organisationId) {
		List<Map<String, Object>> rowsInSpy = new ArrayList<>();
		if (recordReader.rowsToReturn.containsKey(tableName)) {
			rowsInSpy = recordReader.rowsToReturn.get(tableName);
		} else {
			recordReader.rowsToReturn.put(tableName, rowsInSpy);
		}

		Map<String, Object> rowToReturn = new HashMap<>();
		rowToReturn.put("organisation_name_id", nameId);
		rowToReturn.put("organisation_id", organisationId);
		rowToReturn.put("organisation_name", "some english name");
		rowToReturn.put("locale", "en");
		rowsInSpy.add(rowToReturn);
	}

	@Test
	public void testOneNameInDbSameNameInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		addAlternativeName(organisation, "some english name");

		addNameToReturnFromSpy("organisation_name", 234, 678);

		List<DbStatement> dbStatments = alternativeName.handleDbForDataGroup(organisation);
		assertEquals(dbStatments.size(), 0);
	}

	@Test
	public void testOneNameInDbDifferentNameInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		String newAlternativeName = "some other english name";
		addAlternativeName(organisation, newAlternativeName);

		addNameToReturnFromSpy("organisation_name", 234, 678);

		List<DbStatement> dbStatements = alternativeName.handleDbForDataGroup(organisation);
		assertEquals(dbStatements.size(), 1);
		DbStatement dbStatement = dbStatements.get(0);
		assertEquals(dbStatement.getOperation(), "update");
		assertEquals(dbStatement.getTableName(), "organisation_name");

		Map<String, Object> values = dbStatement.getValues();
		assertEquals(values.get("locale"), "en");
		assertEquals(values.get("organisation_id"), 678);

		String lastUpdatedString = extractTimestampFromValues(values);
		assertTrue(lastUpdatedString
				.matches("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\.\\d{1,3}"));
		assertEquals(values.get("organisation_name"), newAlternativeName);

		Map<String, Object> conditions = dbStatement.getConditions();
		assertEquals(conditions.get("organisation_name_id"), 234);

	}

	private String extractTimestampFromValues(Map<String, Object> values) {
		Timestamp lastUpdated = (Timestamp) values.get("last_updated");
		String lastUpdatedString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
				.format(lastUpdated);
		return lastUpdatedString;
	}

	private void addAlternativeName(DataGroup organisation, String name) {
		DataGroupSpy alternativeNameGroup = new DataGroupSpy("alternativeName");
		alternativeNameGroup.addChild(new DataAtomicSpy("organisationName", name));
		alternativeNameGroup.addChild(new DataAtomicSpy("language", "en"));
		organisation.addChild(alternativeNameGroup);
	}

	@Test
	public void testNoNameInDbButNameInDataGroup() {
		alternativeName = new OrganisationAlternativeNameRelatedTable(recordReader, recordDeleter,
				recordCreator, Collections.emptyMap());

		DataGroup organisation = createDataGroupWithId("678");
		DataGroupSpy alternativeNameGroup = new DataGroupSpy("alternativeName");
		String newAlternativeName = "some english name";
		alternativeNameGroup.addChild(new DataAtomicSpy("organisationName", newAlternativeName));
		alternativeNameGroup.addChild(new DataAtomicSpy("language", "en"));
		organisation.addChild(alternativeNameGroup);

		List<DbStatement> dbStatements = alternativeName.handleDbForDataGroup(organisation);

		assertEquals(dbStatements.size(), 1);
		DbStatement dbStatement = dbStatements.get(0);
		assertEquals(dbStatement.getOperation(), "insert");
		assertEquals(dbStatement.getTableName(), "organisation_name");

		Map<String, Object> values = dbStatement.getValues();

		assertEquals(values.get("organisation_name_id"), recordReader.nextVal.get("nextval"));

		assertEquals(values.get("locale"), "en");
		assertEquals(values.get("organisation_id"), 678);

		String lastUpdatedString = extractTimestampFromValues(values);
		assertTrue(lastUpdatedString
				.matches("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\.\\d{1,3}"));
		assertEquals(values.get("organisation_name"), newAlternativeName);

		assertTrue(dbStatement.getConditions().isEmpty());
	}
}
