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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupFactory;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverter;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterSpy;
import se.uu.ub.cora.diva.mixedstorage.db.RecordReaderFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.RecordReaderSpy;
import se.uu.ub.cora.diva.mixedstorage.fedora.DataGroupFactorySpy;

public class DivaDbOrganisationReaderTest {

	private static final String TABLE_NAME = "divaOrganisation";
	private DivaDbToCoraConverterFactorySpy converterFactory;
	private RecordReaderFactorySpy recordReaderFactory;
	private DivaDbOrganisationReader divaDbOrganisationReader;

	private DataGroupFactory dataGroupFactory;

	@BeforeMethod
	public void BeforeMethod() {
		dataGroupFactory = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactory);
		converterFactory = new DivaDbToCoraConverterFactorySpy();
		recordReaderFactory = new RecordReaderFactorySpy();
		divaDbOrganisationReader = DivaDbOrganisationReader
				.usingRecordReaderFactoryAndConverterFactory(recordReaderFactory, converterFactory);
	}

	@Test
	public void testReadOrgansiationFactorDbReader() throws Exception {
		divaDbOrganisationReader.read(TABLE_NAME, "567");
		assertTrue(recordReaderFactory.factorWasCalled);
	}

	@Test
	public void testReadOrgansiationTableRequestedFromReader() throws Exception {
		divaDbOrganisationReader.read(TABLE_NAME, "567");
		RecordReaderSpy recordReader = recordReaderFactory.factored;
		assertEquals(recordReader.usedTableNames.get(0), TABLE_NAME);
		assertEquals(recordReader.usedTableNames.get(1), "divaOrganisationParent");
		assertEquals(recordReader.usedTableNames.get(2), "divaOrganisationPredecessor");
		assertEquals(recordReader.usedTableNames.size(), 3);
	}

	@Test
	public void testReadOrganisationConditionsForOrganisationTable() throws Exception {
		divaDbOrganisationReader.read(TABLE_NAME, "567");
		RecordReaderSpy recordReader = recordReaderFactory.factored;
		Map<String, Object> conditions = recordReader.usedConditionsList.get(0);
		assertEquals(conditions.get("id"), "567");
	}

	@Test
	public void testReadOrganisationConverterIsFactored() throws Exception {
		divaDbOrganisationReader.read(TABLE_NAME, "567");
		DivaDbToCoraConverter divaDbToCoraConverter = converterFactory.factoredConverters.get(0);
		assertNotNull(divaDbToCoraConverter);
	}

	@Test
	public void testReadOrganisationConverterIsCalledWithDataFromDbStorage() throws Exception {
		divaDbOrganisationReader.read(TABLE_NAME, "567");
		RecordReaderSpy recordReader = recordReaderFactory.factored;
		DivaDbToCoraConverterSpy divaDbToCoraConverter = (DivaDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		assertNotNull(divaDbToCoraConverter.mapToConvert);
		assertEquals(recordReader.returnedList.get(0), divaDbToCoraConverter.mapToConvert);
	}

	@Test
	public void testReadOrganisationCallsDatabaseAndReturnsConvertedResultNoPredecessorsNoSuccessorsNoParents()
			throws Exception {
		DataGroup convertedOrganisation = divaDbOrganisationReader.read(TABLE_NAME, "567");

		RecordReaderSpy recordReader = recordReaderFactory.factored;
		assertCorrectTableNamesAndConditionsAreUsedWhenReading(recordReader);

		assertEquals(converterFactory.factoredTypes.get(0), "divaOrganisation");
		assertEquals(converterFactory.factoredTypes.size(), 1);

		assertReadDataIsSentToConverterUsingReadListReadIndexAndConverterIndex(
				recordReader.returnedList, 0, 0);

		DivaDbToCoraConverterSpy organisationConverter = (DivaDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		assertEquals(convertedOrganisation, organisationConverter.convertedDbDataGroup);
	}

	private void assertCorrectTableNamesAndConditionsAreUsedWhenReading(
			RecordReaderSpy recordReader) {
		List<Map<String, Object>> usedConditionsList = recordReader.usedConditionsList;

		assertEquals(recordReader.usedTableNames.get(0), "divaOrganisation");
		assertEquals(usedConditionsList.get(0).get("id"), "567");

		assertEquals(recordReader.usedTableNames.get(1), "divaOrganisationParent");
		assertEquals(usedConditionsList.get(1).get("organisation_id"), "567");

		assertEquals(recordReader.usedTableNames.get(2), "divaOrganisationPredecessor");
		assertEquals(usedConditionsList.get(2).get("organisation_id"), 567);

	}

	private void assertReadDataIsSentToConverterUsingReadListReadIndexAndConverterIndex(
			List<Map<String, Object>> listToReadFrom, int readerIndex, int converterIndex) {
		DivaDbToCoraConverterSpy predecessorConverter = (DivaDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(converterIndex);

		Map<String, Object> firstPredecessorRead = listToReadFrom.get(readerIndex);
		Map<String, Object> mapSentToConverter = predecessorConverter.mapToConvert;
		assertEquals(firstPredecessorRead, mapSentToConverter);
	}

	@Test
	public void testReadOrganisationCanHandleNullPredecessorsAndSuccessorsAndParents()
			throws Exception {
		recordReaderFactory.numOfPredecessorsToReturn = -1;
		DataGroup convertedOrganisation = divaDbOrganisationReader.read(TABLE_NAME, "567");
		RecordReaderSpy recordReader = recordReaderFactory.factored;

		assertCorrectTableNamesAndConditionsAreUsedWhenReading(recordReader);

		DivaDbToCoraConverterSpy organisationConverter = (DivaDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		Map<String, Object> readOrganisation = recordReader.oneRowRead;
		Map<String, Object> mapSentToFirstConverter = organisationConverter.mapToConvert;
		assertEquals(readOrganisation, mapSentToFirstConverter);

		assertFalse(convertedOrganisation.containsChildWithNameInData("from Db converter"));
	}

	@Test
	public void testReadOrganisationCallsDatabaseAndReturnsConvertedResultWithOnePredecessor()
			throws Exception {
		recordReaderFactory.numOfPredecessorsToReturn = 1;
		DataGroup convertedOrganisation = divaDbOrganisationReader.read(TABLE_NAME, "567");
		RecordReaderSpy recordReader = recordReaderFactory.factored;

		assertCorrectTableNamesAndConditionsAreUsedWhenReading(recordReader);
		assertEquals(recordReader.predecessorsToReturn.size(), 1);

		assertEquals(converterFactory.factoredTypes.get(0), "divaOrganisation");
		assertEquals(converterFactory.factoredTypes.get(1), "divaOrganisationPredecessor");
		assertEquals(converterFactory.factoredTypes.size(), 2);

		DivaDbToCoraConverterSpy organisationConverter = (DivaDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		Map<String, Object> readOrganisation = recordReader.oneRowRead;
		Map<String, Object> mapSentToFirstConverter = organisationConverter.mapToConvert;
		assertEquals(readOrganisation, mapSentToFirstConverter);

		List<Map<String, Object>> predecessorsToReturn = recordReader.predecessorsToReturn;
		assertReadDataIsSentToConverterUsingReadListReadIndexAndConverterIndex(predecessorsToReturn,
				0, 1);

		assertTrue(convertedOrganisation.containsChildWithNameInData("from Db converter"));

		List<DataGroup> predecessors = convertedOrganisation
				.getAllGroupsWithNameInData("from Db converter");
		assertEquals(predecessors.get(0).getRepeatId(), "0");
		assertEquals(convertedOrganisation, organisationConverter.convertedDbDataGroup);
	}

	@Test
	public void testReadOrganisationCallsDatabaseAndReturnsConvertedResultWithManyPredecessors()
			throws Exception {
		recordReaderFactory.numOfPredecessorsToReturn = 3;
		recordReaderFactory.noOfRecordsToReturn = 3;
		DataGroup convertedOrganisation = divaDbOrganisationReader.read(TABLE_NAME, "567");
		RecordReaderSpy recordReader = recordReaderFactory.factored;

		assertEquals(recordReader.predecessorsToReturn.size(), 3);
		assertCorrectTableNamesAndConditionsAreUsedWhenReading(recordReader);

		DivaDbToCoraConverterSpy organisationConverter = (DivaDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		Map<String, Object> firstReadResult = recordReader.returnedList.get(0);
		Map<String, Object> mapSentToFirstConverter = organisationConverter.mapToConvert;
		assertEquals(firstReadResult, mapSentToFirstConverter);

		assertEquals(converterFactory.factoredTypes.get(0), "divaOrganisation");
		assertEquals(converterFactory.factoredTypes.get(1), "divaOrganisationPredecessor");
		assertEquals(converterFactory.factoredTypes.get(2), "divaOrganisationPredecessor");
		assertEquals(converterFactory.factoredTypes.get(3), "divaOrganisationPredecessor");
		assertEquals(converterFactory.factoredTypes.size(), 4);

		assertReadDataIsSentToConverterUsingReadListReadIndexAndConverterIndex(
				recordReader.predecessorsToReturn, 0, 1);
		assertReadDataIsSentToConverterUsingReadListReadIndexAndConverterIndex(
				recordReader.predecessorsToReturn, 1, 2);
		assertReadDataIsSentToConverterUsingReadListReadIndexAndConverterIndex(
				recordReader.predecessorsToReturn, 2, 3);

		assertCorrectRepeatIdInAddedChildrenUsingIndex(convertedOrganisation, 0);
		assertCorrectRepeatIdInAddedChildrenUsingIndex(convertedOrganisation, 1);
		assertCorrectRepeatIdInAddedChildrenUsingIndex(convertedOrganisation, 2);

		assertEquals(convertedOrganisation, organisationConverter.convertedDbDataGroup);
	}

	private void assertCorrectRepeatIdInAddedChildrenUsingIndex(DataGroup convertedOrganisation,
			int index) {
		List<DataGroup> predecessors = convertedOrganisation
				.getAllGroupsWithNameInData("from Db converter");
		assertEquals(predecessors.get(index).getRepeatId(), String.valueOf(index));
	}

	@Test
	public void testReadOrganisationCallsDatabaseAndReturnsConvertedResultWithOneParent()
			throws Exception {
		recordReaderFactory.numOfParentsToReturn = 1;
		DataGroup convertedOrganisation = divaDbOrganisationReader.read(TABLE_NAME, "567");
		RecordReaderSpy recordReader = recordReaderFactory.factored;

		assertCorrectTableNamesAndConditionsAreUsedWhenReading(recordReader);
		assertEquals(recordReader.parentsToReturn.size(), 1);

		assertEquals(converterFactory.factoredTypes.get(0), "divaOrganisation");
		assertEquals(converterFactory.factoredTypes.get(1), "divaOrganisationParent");
		assertEquals(converterFactory.factoredTypes.size(), 2);

		DivaDbToCoraConverterSpy organisationConverter = (DivaDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		Map<String, Object> readOrganisation = recordReader.oneRowRead;
		Map<String, Object> mapSentToFirstConverter = organisationConverter.mapToConvert;
		assertEquals(readOrganisation, mapSentToFirstConverter);

		List<Map<String, Object>> parentsToReturn = recordReader.parentsToReturn;
		assertReadDataIsSentToConverterUsingReadListReadIndexAndConverterIndex(parentsToReturn, 0,
				1);

		assertTrue(convertedOrganisation.containsChildWithNameInData("from Db converter"));

		List<DataGroup> parents = convertedOrganisation
				.getAllGroupsWithNameInData("from Db converter");
		assertEquals(parents.get(0).getRepeatId(), "0");
		assertEquals(convertedOrganisation, organisationConverter.convertedDbDataGroup);
	}

	@Test
	public void testReadOrganisationCallsDatabaseAndReturnsConvertedResultWithManyParents()
			throws Exception {
		recordReaderFactory.numOfParentsToReturn = 3;
		recordReaderFactory.noOfRecordsToReturn = 3;
		DataGroup convertedOrganisation = divaDbOrganisationReader.read(TABLE_NAME, "567");
		RecordReaderSpy recordReader = recordReaderFactory.factored;

		assertEquals(recordReader.parentsToReturn.size(), 3);
		assertCorrectTableNamesAndConditionsAreUsedWhenReading(recordReader);

		DivaDbToCoraConverterSpy organisationConverter = (DivaDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		Map<String, Object> firstReadResult = recordReader.returnedList.get(0);
		Map<String, Object> mapSentToFirstConverter = organisationConverter.mapToConvert;
		assertEquals(firstReadResult, mapSentToFirstConverter);

		assertEquals(converterFactory.factoredTypes.get(0), "divaOrganisation");
		assertEquals(converterFactory.factoredTypes.get(1), "divaOrganisationParent");
		assertEquals(converterFactory.factoredTypes.get(2), "divaOrganisationParent");
		assertEquals(converterFactory.factoredTypes.get(3), "divaOrganisationParent");
		assertEquals(converterFactory.factoredTypes.size(), 4);

		assertReadDataIsSentToConverterUsingReadListReadIndexAndConverterIndex(
				recordReader.parentsToReturn, 0, 1);
		assertReadDataIsSentToConverterUsingReadListReadIndexAndConverterIndex(
				recordReader.parentsToReturn, 1, 2);
		assertReadDataIsSentToConverterUsingReadListReadIndexAndConverterIndex(
				recordReader.parentsToReturn, 2, 3);

		assertCorrectRepeatIdInAddedChildrenUsingIndex(convertedOrganisation, 0);
		assertCorrectRepeatIdInAddedChildrenUsingIndex(convertedOrganisation, 1);
		assertCorrectRepeatIdInAddedChildrenUsingIndex(convertedOrganisation, 2);

		assertEquals(convertedOrganisation, organisationConverter.convertedDbDataGroup);
	}
}
