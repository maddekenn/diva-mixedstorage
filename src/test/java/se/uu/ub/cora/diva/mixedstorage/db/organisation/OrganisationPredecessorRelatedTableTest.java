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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
		assertEquals(recordReader.usedConditions.get(0).get("organisation_id"), 678);
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

		addRowToReturnFromSpy("organisation_predecessor", 678, 234, "organisation_predecessor_id");

		predecessor.handleDbForDataGroup(organisation);
		assertCorrecReadPredecessorUsingIndexAndOrgId(0, 678);
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

	private void addRowToReturnFromSpy(String tableName, int organisationId, int predecessorId,
			String dbKey) {
		List<Map<String, Object>> rowsInSpy = new ArrayList<>();
		if (recordReader.rowsToReturn.containsKey(tableName)) {
			rowsInSpy = recordReader.rowsToReturn.get(tableName);
		} else {
			recordReader.rowsToReturn.put(tableName, rowsInSpy);
		}
		Map<String, Object> rowToReturn = new HashMap<>();
		rowToReturn.put("organisation_id", organisationId);
		rowToReturn.put(dbKey, predecessorId);
		rowsInSpy.add(rowToReturn);
	}

	@Test
	public void testOnePredecessorInDbSamePredecessorInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		addPredecessor(organisation, "234", "0");

		addRowToReturnFromSpy("organisation_predecessor", 678, 234, "organisation_predecessor_id");

		predecessor.handleDbForDataGroup(organisation);
		assertCorrecReadPredecessorUsingIndexAndOrgId(0, 678);
		assertFalse(recordDeleter.deleteWasCalled);

		assertFalse(recordCreator.insertWasCalled);

	}

	private void assertCorrecReadPredecessorUsingIndexAndOrgId(int index, int orgId) {
		assertEquals(recordReader.usedTableNames.get(index), "organisation_predecessor");
		assertEquals(recordReader.usedConditions.get(index).get("organisation_id"), orgId);
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

	@Test
	public void testOnePredecessorInDbDifferentPredecessorInDataGroupDeleteAndInsert() {
		DataGroup organisation = createDataGroupWithId("678");
		addPredecessor(organisation, "22234", "0");

		addRowToReturnFromSpy("organisation_predecessor", 678, 234, "organisation_predecessor_id");

		predecessor.handleDbForDataGroup(organisation);
		assertCorrecReadPredecessorUsingIndexAndOrgId(0, 678);

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

	@Test
	public void testNoPredecessorInDbButPredecessorInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		addPredecessor(organisation, "234", "0");

		predecessor.handleDbForDataGroup(organisation);
		assertCorrectDataSentToRecordReader();
		assertFalse(recordDeleter.deleteWasCalled);

		assertTrue(recordCreator.insertWasCalled);
		assertEquals(recordCreator.usedTableNames.size(), 1);
		assertCorrectValuesSentToInsertUsingIndexAndPredecessorId(0, 234);

	}

	private void assertCorrectValuesSentToInsertUsingIndexAndPredecessorId(int index,
			int predecessorId) {
		assertEquals(recordCreator.usedTableNames.get(index), "organisation_predecessor");
		Map<String, Object> values = recordCreator.listOfValues.get(index);

		assertEquals(values.get("organisation_id"), 678);
		assertEquals(values.get("organisation_predecessor_id"), predecessorId);
		assertEquals(values.get("organisation_predecessor_id"), predecessorId);

	}

	@Test
	public void testMultiplePredecessorsInDbDifferentAndSamePredecessorsInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		addMultiplePredecessors(organisation);
		addMultipleRowsToReturn();

		predecessor.handleDbForDataGroup(organisation);

		assertCorrecReadPredecessorUsingIndexAndOrgId(0, 678);
		assertTwoPredecessorsWasDeleted();
		assertTwoPredecessorsWasCreated();

	}

	private void addMultiplePredecessors(DataGroup organisation) {
		addPredecessor(organisation, "23", "0");
		addPredecessor(organisation, "234", "1");
		addPredecessor(organisation, "22234", "2");
		addPredecessor(organisation, "44444", "2");
	}

	private void addMultipleRowsToReturn() {
		addRowToReturnFromSpy("organisation_predecessor", 678, 234, "organisation_predecessor_id");
		addRowToReturnFromSpy("organisation_predecessor", 678, 22234,
				"organisation_predecessor_id");
		addRowToReturnFromSpy("organisation_predecessor", 678, 2444, "organisation_predecessor_id");
		addRowToReturnFromSpy("organisation_predecessor", 678, 2222, "organisation_predecessor_id");
	}

	private void assertTwoPredecessorsWasDeleted() {
		assertEquals(recordDeleter.listOfUsedConditions.size(), 2);
		Map<String, Object> conditionsForFirstDelete = recordDeleter.listOfUsedConditions.get(0);
		assertEquals(recordDeleter.usedTableNames.get(0), "organisation_predecessor");
		assertEquals(conditionsForFirstDelete.get("organisation_id"), 678);
		assertEquals(conditionsForFirstDelete.get("organisation_predecessor_id"), 2444);

		Map<String, Object> conditionsForSecondDelete = recordDeleter.listOfUsedConditions.get(1);
		assertEquals(recordDeleter.usedTableNames.get(1), "organisation_predecessor");
		assertEquals(conditionsForSecondDelete.get("organisation_id"), 678);
		assertEquals(conditionsForSecondDelete.get("organisation_predecessor_id"), 2222);
	}

	private void assertTwoPredecessorsWasCreated() {
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

	/***************************
	 * With description
	 **************************************************/
	@Test
	public void testNoPredecessorInDbButPredecessorWithDescriptionInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		addPredecessorWithDescription(organisation, "234", "0");

		predecessor.handleDbForDataGroup(organisation);
		assertCorrecReadPredecessorUsingIndexAndOrgId(0, 678);

		assertFalse(recordDeleter.deleteWasCalled);

		assertTrue(recordCreator.insertWasCalled);
		assertCorrectValuesSentToInsertUsingIndexAndPredecessorId(0, 234);

		assertEquals(recordCreator.usedTableNames.get(1), "organisation_predecessor_description");
		Map<String, Object> values = recordCreator.listOfValues.get(1);
		assertEquals(values.get("organisation_id"), 678);
		assertEquals(values.get("predecessor_id"), 234);
	}

	@Test
	public void testNoPredecessorInDataGroupButPredecessorWithDescriptionInDb() {
		DataGroup organisation = createDataGroupWithId("678");

		addRowToReturnFromSpy("organisation_predecessor", 678, 234, "organisation_predecessor_id");
		addRowToReturnFromSpy("organisation_predecessor_description", 678, 234, "predecessor_id");

		predecessor.handleDbForDataGroup(organisation);

		assertEquals(recordReader.usedTableNames.size(), 1);
		assertCorrecReadPredecessorUsingIndexAndOrgId(0, 678);

		assertEquals(recordDeleter.usedTableNames.size(), 2);
		assertFirstDeleteWasPredecessorDescription();

		assertEquals(recordDeleter.usedTableNames.get(1), "organisation_predecessor");
		assertEquals(recordDeleter.listOfUsedConditions.get(1).get("organisation_id"), 678);
		assertEquals(recordDeleter.listOfUsedConditions.get(1).get("organisation_predecessor_id"),
				234);

		assertFalse(recordCreator.insertWasCalled);
	}

	private void assertFirstDeleteWasPredecessorDescription() {
		assertEquals(recordDeleter.usedTableNames.get(0), "organisation_predecessor_description");
		assertEquals(recordDeleter.listOfUsedConditions.get(0).get("organisation_id"), 678);
		assertEquals(recordDeleter.listOfUsedConditions.get(0).get("predecessor_id"), 234);
	}

	@Test
	public void testOnePredecessorInDbSamePredecessorInDataGroupSameComment() {
		DataGroup organisation = createDataGroupWithId("678");
		addPredecessorWithDescription(organisation, "234", "0");

		addRowToReturnFromSpy("organisation_predecessor", 678, 234, "organisation_predecessor_id");
		addRowToReturnFromSpy("organisation_predecessor_description", 678, 234, "predecessor_id");
		Map<String, Object> descriptionMapInSpy = recordReader.rowsToReturn
				.get("organisation_predecessor_description").get(0);
		descriptionMapInSpy.put("description", "some description");

		predecessor.handleDbForDataGroup(organisation);

		assertPredecessorAndDescriptionWasRead();
		assertFalse(recordDeleter.deleteWasCalled);
		assertFalse(recordCreator.insertWasCalled);
	}

	@Test
	public void testOnePredecessorInDbSamePredecessorInDataGroupDifferentComment() {
		DataGroup organisation = createDataGroupWithId("678");
		addPredecessorWithDescription(organisation, "234", "0");

		addRowToReturnFromSpy("organisation_predecessor", 678, 234, "organisation_predecessor_id");
		addRowToReturnFromSpy("organisation_predecessor_description", 678, 234, "predecessor_id");
		Map<String, Object> descriptionMapInSpy = recordReader.rowsToReturn
				.get("organisation_predecessor_description").get(0);
		descriptionMapInSpy.put("description", "some OTHER description");

		predecessor.handleDbForDataGroup(organisation);

		assertPredecessorAndDescriptionWasRead();
		assertDescriptionWasDeleted();
		assertNewDescriptionWasCreated();
	}

	@Test
	public void testOnePredecessorInDbSamePredecessorInDataGroupNoCommentInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		addPredecessor(organisation, "234", "0");

		addRowToReturnFromSpy("organisation_predecessor", 678, 234, "organisation_predecessor_id");
		addRowToReturnFromSpy("organisation_predecessor_description", 678, 234, "predecessor_id");
		Map<String, Object> descriptionMapInSpy = recordReader.rowsToReturn
				.get("organisation_predecessor_description").get(0);
		descriptionMapInSpy.put("description", "some OTHER description");

		predecessor.handleDbForDataGroup(organisation);

		assertPredecessorAndDescriptionWasRead();
		assertDescriptionWasDeleted();
		assertFalse(recordCreator.insertWasCalled);
	}

	@Test
	public void testOnePredecessorInDbSamePredecessorInDataGroupNoCommentInDb() {
		DataGroup organisation = createDataGroupWithId("678");
		addPredecessorWithDescription(organisation, "234", "0");

		addRowToReturnFromSpy("organisation_predecessor", 678, 234, "organisation_predecessor_id");

		predecessor.handleDbForDataGroup(organisation);

		assertPredecessorAndDescriptionWasRead();
		assertFalse(recordDeleter.deleteWasCalled);
		assertNewDescriptionWasCreated();
	}

	private void assertPredecessorAndDescriptionWasRead() {
		assertEquals(recordReader.usedTableNames.size(), 2);
		assertCorrecReadPredecessorUsingIndexAndOrgId(0, 678);

		assertEquals(recordReader.usedTableNames.get(1), "organisation_predecessor_description");
		assertEquals(recordReader.usedConditions.get(1).get("organisation_id"), 678);
		assertEquals(recordReader.usedConditions.get(1).get("predecessor_id"), 234);
	}

	private void assertDescriptionWasDeleted() {
		assertTrue(recordDeleter.deleteWasCalled);
		assertEquals(recordDeleter.usedTableNames.size(), 1);
		assertFirstDeleteWasPredecessorDescription();
	}

	private void assertNewDescriptionWasCreated() {
		assertEquals(recordCreator.usedTableNames.size(), 1);
		assertEquals(recordCreator.usedTableNames.get(0), "organisation_predecessor_description");
		assertEquals(recordCreator.listOfValues.get(0).get("predecessor_id"), 234);
		assertEquals(recordCreator.listOfValues.get(0).get("description"), "some description");

		Timestamp lastUpdated = (Timestamp) recordCreator.listOfValues.get(0).get("last_updated");

		String lastUpdatedString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
				.format(lastUpdated);
		assertTrue(lastUpdatedString
				.matches("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\.\\d{1,3}"));

		assertEquals(recordReader.sequenceName, "organisation_predecessor_description_sequence");
		assertEquals(recordCreator.values.get("organisation_predecessor_id"),
				recordReader.nextVal.get("nextval"));
	}

	@Test
	public void testOnePredecessorInDbWithDescriptionSameAndAddedPredecessorInDataGroupWithDescription() {
		DataGroup organisation = createDataGroupWithId("678");
		addPredecessorWithDescription(organisation, "22234", "0");
		addPredecessorWithDescription(organisation, "234", "1");

		addRowToReturnFromSpy("organisation_predecessor", 678, 234, "organisation_predecessor_id");
		addRowToReturnFromSpy("organisation_predecessor_description", 678, 234, "predecessor_id");

		Map<String, Object> descriptionMapInSpy = recordReader.rowsToReturn
				.get("organisation_predecessor_description").get(0);
		descriptionMapInSpy.put("description", "some description");

		predecessor.handleDbForDataGroup(organisation);
		assertCorrecReadPredecessorUsingIndexAndOrgId(0, 678);

		assertFalse(recordDeleter.deleteWasCalled);
		assertTrue(recordCreator.insertWasCalled);
		Map<String, Object> conditionsForFirstCreate = recordCreator.listOfValues.get(0);

		assertEquals(recordCreator.usedTableNames.get(0), "organisation_predecessor");
		assertEquals(conditionsForFirstCreate.get("organisation_id"), 678);
		assertEquals(conditionsForFirstCreate.get("organisation_predecessor_id"), 22234);

		assertEquals(recordCreator.usedTableNames.get(1), "organisation_predecessor_description");
		Map<String, Object> conditionsForDescription = recordCreator.listOfValues.get(1);
		assertEquals(conditionsForDescription.get("organisation_id"), 678);
		assertEquals(conditionsForDescription.get("predecessor_id"), 22234);
		assertEquals(conditionsForDescription.get("organisation_predecessor_id"),
				recordReader.nextVal.get("nextval"));
		Timestamp lastUpdated = (Timestamp) conditionsForDescription.get("last_updated");

		String lastUpdatedString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
				.format(lastUpdated);
		assertTrue(lastUpdatedString
				.matches("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\.\\d{1,3}"));
		assertEquals(conditionsForDescription.get("description"), "some description");

	}

}
