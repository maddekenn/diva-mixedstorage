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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.db.RecordCreatorSpy;
import se.uu.ub.cora.diva.mixedstorage.db.RecordDeleterSpy;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTable;

public class OrganisationParentRelatedTableTest {

	private RecordReaderRelatedTableSpy recordReader;
	private RecordDeleterSpy recordDeleter;
	private RecordCreatorSpy recordCreator;
	private RelatedTable parent;

	@BeforeMethod
	public void setUp() {
		recordReader = new RecordReaderRelatedTableSpy();
		recordDeleter = new RecordDeleterSpy();
		recordCreator = new RecordCreatorSpy();
		parent = new OrganisationParentRelatedTable(recordReader, recordDeleter, recordCreator);

	}

	@Test
	public void testInit() {
		DataGroup organisation = createDataGroupWithId("678");
		parent.handleDbForDataGroup(organisation);
		assertCorrectDataSentToRecordReader();
	}

	private DataGroup createDataGroupWithId(String id) {
		DataGroup dataGroup = new DataGroupSpy("organisation");
		DataGroupSpy recordInfo = new DataGroupSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", id));
		dataGroup.addChild(recordInfo);
		return dataGroup;
	}

	private void assertCorrectDataSentToRecordReader() {
		assertEquals(recordReader.usedTableName, "organisation_parent");
		assertEquals(recordReader.usedConditions.get("organisation_id"), 678);
	}

	@Test
	public void testNoParentInDbNoParentInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		parent.handleDbForDataGroup(organisation);
		assertCorrectDataSentToRecordReader();
		assertFalse(recordDeleter.deleteWasCalled);
		assertFalse(recordCreator.insertWasCalled);
	}

	@Test
	public void testOneParentInDbButNoParentInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");

		addRowToReturnFromSpy(678, 234);

		parent.handleDbForDataGroup(organisation);
		assertCorrectDataSentToRecordReader();
		assertTrue(recordDeleter.deleteWasCalled);

		assertEquals(recordDeleter.usedTableName, "organisation_parent");
		assertEquals(recordDeleter.usedConditions.get("organisation_id"), 678);
		assertEquals(recordDeleter.usedConditions.get("organisation_parent_id"), 234);

		assertFalse(recordCreator.insertWasCalled);

	}

	private void addRowToReturnFromSpy(int organisationId, int parentId) {
		Map<String, Object> rowToReturn = new HashMap<>();
		rowToReturn.put("organisation_id", organisationId);
		rowToReturn.put("organisation_parent_id", parentId);
		recordReader.rowsToReturn.add(rowToReturn);
	}

	@Test
	public void testOneParentInDbSameParentInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		addParent(organisation, "234", "0");

		addRowToReturnFromSpy(678, 234);

		parent.handleDbForDataGroup(organisation);
		assertCorrectDataSentToRecordReader();
		assertFalse(recordDeleter.deleteWasCalled);

		assertFalse(recordCreator.insertWasCalled);

	}

	private void addParent(DataGroup organisation, String parentId, String repeatId) {
		DataGroup parent = new DataGroupSpy("parentOrganisation");
		parent.setRepeatId(repeatId);
		DataGroupSpy parentLink = new DataGroupSpy("organisationLink");
		parentLink.addChild(new DataAtomicSpy("linkedRecordType", "divaOrganisation"));
		parentLink.addChild(new DataAtomicSpy("linkedRecordId", parentId));
		parent.addChild(parentLink);
		organisation.addChild(parent);
	}

	@Test
	public void testOneParentInDbDifferentParentInDataGroupDeleteAndInsert() {
		DataGroup organisation = createDataGroupWithId("678");
		addParent(organisation, "22234", "0");

		addRowToReturnFromSpy(678, 234);

		parent.handleDbForDataGroup(organisation);
		assertCorrectDataSentToRecordReader();

		assertTrue(recordDeleter.deleteWasCalled);
		Map<String, Object> conditionsForFirstDelete = recordDeleter.listOfUsedConditions.get(0);
		assertEquals(recordDeleter.usedTableNames.get(0), "organisation_parent");
		assertEquals(conditionsForFirstDelete.get("organisation_id"), 678);
		assertEquals(conditionsForFirstDelete.get("organisation_parent_id"), 234);

		assertTrue(recordCreator.insertWasCalled);
		Map<String, Object> conditionsForFirstCreate = recordCreator.listOfValues.get(0);

		assertEquals(recordCreator.usedTableNames.get(0), "organisation_parent");
		assertEquals(conditionsForFirstCreate.get("organisation_id"), 678);
		assertEquals(conditionsForFirstCreate.get("organisation_parent_id"), 22234);

	}

	private void assertCorrectValuesSentToInsert(int parentId) {
		assertEquals(recordCreator.usedTableName, "organisation_parent");
		assertEquals(recordCreator.values.get("organisation_id"), 678);
		assertEquals(recordCreator.values.get("organisation_parent_id"), parentId);
	}

	@Test
	public void testNoParentInDbButNameInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		addParent(organisation, "234", "0");

		parent.handleDbForDataGroup(organisation);
		assertCorrectDataSentToRecordReader();
		assertFalse(recordDeleter.deleteWasCalled);

		assertTrue(recordCreator.insertWasCalled);
		assertCorrectValuesSentToInsert(234);
	}

	@Test
	public void testMultipleParentsInDbDifferentAndSameNamesInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		addParent(organisation, "23", "0");
		addParent(organisation, "234", "1");
		addParent(organisation, "22234", "2");
		addParent(organisation, "44444", "2");

		addRowToReturnFromSpy(678, 234);
		addRowToReturnFromSpy(678, 22234);
		addRowToReturnFromSpy(678, 2444);
		addRowToReturnFromSpy(678, 2222);

		parent.handleDbForDataGroup(organisation);
		assertCorrectDataSentToRecordReader();

		assertTrue(recordDeleter.deleteWasCalled);
		assertEquals(recordDeleter.listOfUsedConditions.size(), 2);
		Map<String, Object> conditionsForFirstDelete = recordDeleter.listOfUsedConditions.get(0);
		assertEquals(recordDeleter.usedTableNames.get(0), "organisation_parent");
		assertEquals(conditionsForFirstDelete.get("organisation_id"), 678);
		assertEquals(conditionsForFirstDelete.get("organisation_parent_id"), 2444);

		Map<String, Object> conditionsForSecondDelete = recordDeleter.listOfUsedConditions.get(1);
		assertEquals(recordDeleter.usedTableNames.get(1), "organisation_parent");
		assertEquals(conditionsForSecondDelete.get("organisation_id"), 678);
		assertEquals(conditionsForSecondDelete.get("organisation_parent_id"), 2222);

		assertTrue(recordCreator.insertWasCalled);
		assertEquals(recordCreator.listOfValues.size(), 2);

		Map<String, Object> conditionsForFirstCreate = recordCreator.listOfValues.get(0);
		assertEquals(recordCreator.usedTableNames.get(0), "organisation_parent");
		assertEquals(conditionsForFirstCreate.get("organisation_id"), 678);
		assertEquals(conditionsForFirstCreate.get("organisation_parent_id"), 23);

		Map<String, Object> conditionsForSecondCreate = recordCreator.listOfValues.get(1);
		assertEquals(recordCreator.usedTableNames.get(1), "organisation_parent");
		assertEquals(conditionsForSecondCreate.get("organisation_id"), 678);
		assertEquals(conditionsForSecondCreate.get("organisation_parent_id"), 44444);

	}
}
