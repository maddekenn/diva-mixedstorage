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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
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
		alternativeName = new OrganisationAlternativeNameRelatedTable(recordReader, recordDeleter,
				recordCreator);

	}

	@Test
	public void testInit() {
		DataGroup organisation = createDataGroupWithId("678");
		alternativeName.handleDbForDataGroup(organisation);
		assertCorrectDataSentToRecordReader();
	}

	private DataGroup createDataGroupWithId(String id) {
		DataGroup dataGroup = new DataGroupSpy("organisation");
		DataGroupSpy recordInfo = new DataGroupSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", id));
		dataGroup.addChild(recordInfo);
		return dataGroup;
	}

	@Test
	public void testNoNameInDbNoNameInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		alternativeName.handleDbForDataGroup(organisation);
		assertCorrectDataSentToRecordReader();
		assertFalse(recordDeleter.deleteWasCalled);
		assertFalse(recordCreator.insertWasCalled);
	}

	@Test
	public void testNoNameInDbIncompleteNameInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		DataGroupSpy alternativeNameGroup = new DataGroupSpy("alternativeName");
		organisation.addChild(alternativeNameGroup);

		alternativeName.handleDbForDataGroup(organisation);
		assertCorrectDataSentToRecordReader();
		assertFalse(recordDeleter.deleteWasCalled);
		assertFalse(recordCreator.insertWasCalled);
	}

	@Test
	public void testOneNameInDbButNoNameInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");

		addNameToReturnFromSpy("organisation_name", 234, 678);

		alternativeName.handleDbForDataGroup(organisation);
		assertCorrectDataSentToRecordReader();
		assertTrue(recordDeleter.deleteWasCalled);

		assertEquals(recordDeleter.usedTableName, "organisation_name");
		assertEquals(recordDeleter.usedConditions.get("organisation_name_id"), 234);

		assertFalse(recordCreator.insertWasCalled);

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

		alternativeName.handleDbForDataGroup(organisation);
		assertCorrectDataSentToRecordReader();
		assertFalse(recordDeleter.deleteWasCalled);

		assertFalse(recordCreator.insertWasCalled);

	}

	private void assertCorrectDataSentToRecordReader() {
		assertEquals(recordReader.usedTableName, "organisation_name");
		assertEquals(recordReader.usedConditions.get("locale"), "en");
		assertEquals(recordReader.usedConditions.get("organisation_id"), 678);
	}

	@Test
	public void testOneNameInDbDifferentNameInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		addAlternativeName(organisation, "some other english name");

		addNameToReturnFromSpy("organisation_name", 234, 678);

		alternativeName.handleDbForDataGroup(organisation);
		assertCorrectDataSentToRecordReader();

		assertTrue(recordDeleter.deleteWasCalled);

		assertTrue(recordCreator.insertWasCalled);
		assertCorrectValuesSentToInsert("some other english name");

	}

	private void assertCorrectValuesSentToInsert(String name) {
		assertEquals(recordReader.sequenceName, "name_sequence");
		assertEquals(recordCreator.usedTableName, "organisation_name");
		assertEquals(recordCreator.values.get("organisation_name_id"),
				recordReader.nextVal.get("nextval"));
		assertEquals(recordCreator.values.get("locale"), "en");
		assertEquals(recordCreator.values.get("organisation_id"), 678);

		String lastUpdated = (String) recordCreator.values.get("last_updated");
		assertTrue(lastUpdated
				.matches("timestamp '\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\.\\d{1,3}'"));

		assertEquals(recordCreator.values.get("organisation_name"), name);
	}

	private void addAlternativeName(DataGroup organisation, String name) {
		DataGroupSpy alternativeNameGroup = new DataGroupSpy("alternativeName");
		alternativeNameGroup.addChild(new DataAtomicSpy("organisationName", name));
		alternativeNameGroup.addChild(new DataAtomicSpy("language", "en"));
		organisation.addChild(alternativeNameGroup);
	}

	@Test
	public void testNoNameInDbButNameInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		DataGroupSpy alternativeNameGroup = new DataGroupSpy("alternativeName");
		alternativeNameGroup.addChild(new DataAtomicSpy("organisationName", "some english name"));
		alternativeNameGroup.addChild(new DataAtomicSpy("language", "en"));
		organisation.addChild(alternativeNameGroup);

		alternativeName.handleDbForDataGroup(organisation);
		assertCorrectDataSentToRecordReader();
		assertFalse(recordDeleter.deleteWasCalled);

		assertTrue(recordCreator.insertWasCalled);
		assertCorrectValuesSentToInsert("some english name");
	}
}
