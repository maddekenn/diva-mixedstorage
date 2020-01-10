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

public class OrganisationPredecessorRelatedTableTest {

	private RecordReaderRelatedTableSpy recordReader;
	private RecordDeleterSpy recordDeleter;
	private RecordCreatorSpy recordCreator;
	private RelatedTable predecessor;

	@BeforeMethod
	public void setUp() {
		recordReader = new RecordReaderRelatedTableSpy();
		recordDeleter = new RecordDeleterSpy();
		recordCreator = new RecordCreatorSpy();
		predecessor = new OrganisationPredecessorRelatedTable(recordReader, recordDeleter,
				recordCreator);

	}

	@Test
	public void testInit() {
		DataGroup organisation = createDataGroupWithId("678");
		predecessor.handleDbForDataGroup(organisation);
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
		assertEquals(recordReader.usedTableName, "organisation_predecessor");
		assertEquals(recordReader.usedConditions.get("organisation_id"), 678);
	}

	@Test
	public void testNoPredecessorInDbNoPredecessorInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		predecessor.handleDbForDataGroup(organisation);
		assertCorrectDataSentToRecordReader();
		assertFalse(recordDeleter.deleteWasCalled);
		assertFalse(recordCreator.insertWasCalled);
	}

	@Test
	public void testOnePredecessorInDbButNoPredecessorInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");

		addRowToReturnFromSpy(678, 234);

		predecessor.handleDbForDataGroup(organisation);
		assertCorrectDataSentToRecordReader();
		assertTrue(recordDeleter.deleteWasCalled);

		assertEquals(recordDeleter.usedTableNames.size(), 2);
		assertEquals(recordDeleter.usedTableNames.get(0), "organisation_predecessor_description");
		Map<String, Object> firstConditionsMap = recordDeleter.listOfUsedConditions.get(0);
		assertEquals(firstConditionsMap.get("organisation_id"), 678);
		assertEquals(firstConditionsMap.get("predecessor_id"), 234);

		assertEquals(recordDeleter.usedTableName, "organisation_predecessor");
		assertEquals(recordDeleter.usedConditions.get("organisation_id"), 678);
		assertEquals(recordDeleter.usedConditions.get("organisation_predecessor_id"), 234);

		assertFalse(recordCreator.insertWasCalled);

	}

	private void addRowToReturnFromSpy(int organisationId, int predecessorId) {
		Map<String, Object> rowToReturn = new HashMap<>();
		rowToReturn.put("organisation_id", organisationId);
		rowToReturn.put("organisation_predecessor_id", predecessorId);
		recordReader.rowsToReturn.add(rowToReturn);
	}

	@Test
	public void testOnePredecessorInDbSamePredecessorInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		addPredecessor(organisation, "234", "0");

		addRowToReturnFromSpy(678, 234);

		predecessor.handleDbForDataGroup(organisation);
		assertCorrectDataSentToRecordReader();
		assertFalse(recordDeleter.deleteWasCalled);

		assertFalse(recordCreator.insertWasCalled);

	}

	private void addPredecessor(DataGroup organisation, String predecessorId, String repeatId) {
		DataGroup predecessorGroup = createPredecessorGroupWithRepeatId(repeatId);
		DataGroupSpy predecessorLink = createPredecessorLink(predecessorId);
		predecessorGroup.addChild(predecessorLink);
		organisation.addChild(predecessorGroup);
	}

	private DataGroup createPredecessorGroupWithRepeatId(String repeatId) {
		DataGroup predecessorGroup = new DataGroupSpy("formerName");
		predecessorGroup.setRepeatId(repeatId);
		return predecessorGroup;
	}

	private DataGroupSpy createPredecessorLink(String predecessorId) {
		DataGroupSpy predecessorLink = new DataGroupSpy("organisationLink");
		predecessorLink.addChild(new DataAtomicSpy("linkedRecordType", "divaOrganisation"));
		predecessorLink.addChild(new DataAtomicSpy("linkedRecordId", predecessorId));
		return predecessorLink;
	}

	private void addPredecessorWithDescription(DataGroup organisation, String predecessorId,
			String repeatId) {
		DataGroup predecessorGroup = createPredecessorGroupWithRepeatId(repeatId);
		DataGroupSpy predecessorLink = createPredecessorLink(predecessorId);
		predecessorGroup.addChild(predecessorLink);
		predecessorGroup.addChild(new DataAtomicSpy("organisationComment", "some description"));
		organisation.addChild(predecessorGroup);
	}

	// @Test
	// public void testOnePredecessorInDbSamePredecessorInDataGroupSameComment() {
	// DataGroup organisation = createDataGroupWithId("678");
	// addPredecessor(organisation, "234", "0");
	//
	// addRowToReturnFromSpy(678, 234);
	//
	// predecessor.handleDbForDataGroup(organisation);
	// assertCorrectDataSentToRecordReader();
	// assertFalse(recordDeleter.deleteWasCalled);
	//
	// assertFalse(recordCreator.insertWasCalled);
	//
	// }

	@Test
	public void testOnePredecessorInDbDifferentPredecessorInDataGroupDeleteAndInsert() {
		DataGroup organisation = createDataGroupWithId("678");
		addPredecessor(organisation, "22234", "0");

		addRowToReturnFromSpy(678, 234);

		predecessor.handleDbForDataGroup(organisation);
		assertCorrectDataSentToRecordReader();

		assertTrue(recordDeleter.deleteWasCalled);
		Map<String, Object> conditionsForFirstDelete = recordDeleter.listOfUsedConditions.get(0);
		assertEquals(recordDeleter.usedTableNames.get(0), "organisation_predecessor");
		assertEquals(conditionsForFirstDelete.get("organisation_id"), 678);
		assertEquals(conditionsForFirstDelete.get("organisation_predecessor_id"), 234);

		assertTrue(recordCreator.insertWasCalled);
		Map<String, Object> conditionsForFirstCreate = recordCreator.listOfValues.get(0);

		assertEquals(recordCreator.usedTableNames.get(0), "organisation_predecessor");
		assertEquals(conditionsForFirstCreate.get("organisation_id"), 678);
		assertEquals(conditionsForFirstCreate.get("organisation_predecessor_id"), 22234);

	}

	// private void assertCorrectValuesSentToInsert(int predecessorId) {
	// assertEquals(recordCreator.usedTableName, "organisation_predecessor");
	// assertEquals(recordCreator.values.get("organisation_id"), 678);
	// assertEquals(recordCreator.values.get("organisation_predecessor_id"), predecessorId);
	// }

	@Test
	public void testNoPredecessorInDbButPredecessorInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		addPredecessor(organisation, "234", "0");

		predecessor.handleDbForDataGroup(organisation);
		assertCorrectDataSentToRecordReader();
		assertFalse(recordDeleter.deleteWasCalled);

		assertTrue(recordCreator.insertWasCalled);
		assertEquals(recordCreator.usedTableNames.size(), 1);
		assertCorrectValuesSentToInsert(234, 0);

	}

	@Test
	public void testNoPredecessorInDbButPredecessorWithDescriptionInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		addPredecessorWithDescription(organisation, "234", "0");

		predecessor.handleDbForDataGroup(organisation);
		assertCorrectDataSentToRecordReader();
		assertFalse(recordDeleter.deleteWasCalled);

		assertTrue(recordCreator.insertWasCalled);
		assertCorrectValuesSentToInsert(234, 0);

		assertEquals(recordCreator.usedTableNames.get(1), "organisation_predecessor_description");
		Map<String, Object> values = recordCreator.listOfValues.get(1);
		assertEquals(values.get("organisation_id"), 678);
		assertEquals(values.get("predecessor_id"), 234);
		// assertEquals(values.get("description"), "some description");
		// assertEquals(values.get("last_updated"), "");
		// assertEquals(values.get("organisation_predecessor_id"), "some value rendered from spy");
	}

	private void assertCorrectValuesSentToInsert(int predecessorId, int index) {
		assertEquals(recordCreator.usedTableNames.get(index), "organisation_predecessor");
		Map<String, Object> values = recordCreator.listOfValues.get(index);

		assertEquals(values.get("organisation_id"), 678);
		assertEquals(values.get("organisation_predecessor_id"), predecessorId);
		assertEquals(values.get("organisation_predecessor_id"), predecessorId);

	}

	@Test
	public void testMultiplePredecessorsInDbDifferentAndSamePredecessorsInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		addPredecessor(organisation, "23", "0");
		addPredecessor(organisation, "234", "1");
		addPredecessor(organisation, "22234", "2");
		addPredecessor(organisation, "44444", "2");

		addRowToReturnFromSpy(678, 234);
		addRowToReturnFromSpy(678, 22234);
		addRowToReturnFromSpy(678, 2444);
		addRowToReturnFromSpy(678, 2222);

		predecessor.handleDbForDataGroup(organisation);
		assertCorrectDataSentToRecordReader();

		assertTrue(recordDeleter.deleteWasCalled);
		assertEquals(recordDeleter.listOfUsedConditions.size(), 2);
		Map<String, Object> conditionsForFirstDelete = recordDeleter.listOfUsedConditions.get(0);
		assertEquals(recordDeleter.usedTableNames.get(0), "organisation_predecessor");
		assertEquals(conditionsForFirstDelete.get("organisation_id"), 678);
		assertEquals(conditionsForFirstDelete.get("organisation_predecessor_id"), 2444);

		Map<String, Object> conditionsForSecondDelete = recordDeleter.listOfUsedConditions.get(1);
		assertEquals(recordDeleter.usedTableNames.get(1), "organisation_predecessor");
		assertEquals(conditionsForSecondDelete.get("organisation_id"), 678);
		assertEquals(conditionsForSecondDelete.get("organisation_predecessor_id"), 2222);

		assertTrue(recordCreator.insertWasCalled);
		assertEquals(recordCreator.listOfValues.size(), 2);

		Map<String, Object> conditionsForFirstCreate = recordCreator.listOfValues.get(0);
		assertEquals(recordCreator.usedTableNames.get(0), "organisation_predecessor");
		assertEquals(conditionsForFirstCreate.get("organisation_id"), 678);
		assertEquals(conditionsForFirstCreate.get("organisation_predecessor_id"), 23);

		Map<String, Object> conditionsForSecondCreate = recordCreator.listOfValues.get(1);
		assertEquals(recordCreator.usedTableNames.get(1), "organisation_predecessor");
		assertEquals(conditionsForSecondCreate.get("organisation_id"), 678);
		assertEquals(conditionsForSecondCreate.get("organisation_predecessor_id"), 44444);

	}
}
