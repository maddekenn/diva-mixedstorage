package se.uu.ub.cora.diva.mixedstorage.db;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.bookkeeper.data.DataGroup;

public class DivaDbToCoraOrganisationTest {

	private static final String TABLE_NAME = "divaOrganisation";
	private DivaDbToCoraConverterFactorySpy converterFactory;
	private RecordReaderFactorySpy recordReaderFactory;
	private DivaDbToCoraOrganisation toCoraOrganisation;

	@BeforeMethod
	public void BeforeMethod() {
		converterFactory = new DivaDbToCoraConverterFactorySpy();
		recordReaderFactory = new RecordReaderFactorySpy();
		toCoraOrganisation = DivaDbToCoraOrganisation
				.usingRecordReaderFactoryAndConverterFactory(recordReaderFactory, converterFactory);
	}

	@Test
	public void testReadOrgansiationFactorDbReader() throws Exception {
		toCoraOrganisation.readAndConvertOneRow(TABLE_NAME, "someId");
		assertTrue(recordReaderFactory.factorWasCalled);
	}

	@Test
	public void testReadOrgansiationTableRequestedFromReader() throws Exception {
		toCoraOrganisation.readAndConvertOneRow(TABLE_NAME, "someId");
		RecordReaderSpy recordReader = recordReaderFactory.factored;
		assertEquals(recordReader.usedTableNames.get(0), TABLE_NAME);
		assertEquals(recordReader.usedTableNames.get(1), "divaOrganisationPredecessor");
		assertEquals(recordReader.usedTableNames.get(2), "divaOrganisationPredecessor");

		assertEquals(recordReader.usedTableNames.size(), 3);
	}

	@Test
	public void testReadOrganisationConditionsForOrganisationTable() throws Exception {
		toCoraOrganisation.readAndConvertOneRow(TABLE_NAME, "someId");
		RecordReaderSpy recordReader = recordReaderFactory.factored;
		Map<String, Object> conditions = recordReader.usedConditionsList.get(0);
		assertEquals(conditions.get("id"), "someId");
	}

	@Test
	public void testReadOrganisationConverterIsFactored() throws Exception {
		toCoraOrganisation.readAndConvertOneRow(TABLE_NAME, "someId");
		DivaDbToCoraConverter divaDbToCoraConverter = converterFactory.factoredConverters.get(0);
		assertNotNull(divaDbToCoraConverter);
	}

	@Test
	public void testReadOrganisationConverterIsCalledWithDataFromDbStorage() throws Exception {
		toCoraOrganisation.readAndConvertOneRow(TABLE_NAME, "someId");
		RecordReaderSpy recordReader = recordReaderFactory.factored;
		DivaDbToCoraConverterSpy divaDbToCoraConverter = (DivaDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		assertNotNull(divaDbToCoraConverter.mapToConvert);
		assertEquals(recordReader.returnedList.get(0), divaDbToCoraConverter.mapToConvert);
	}

	@Test
	public void testReadOrganisationCallsDatabaseAndReturnsConvertedResultNoPredecessorsNoSuccessors()
			throws Exception {
		DataGroup convertedOrganisation = toCoraOrganisation.readAndConvertOneRow(TABLE_NAME,
				"someId");

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
		assertEquals(usedConditionsList.get(0).get("id"), "someId");

		assertEquals(recordReader.usedTableNames.get(1), "divaOrganisationPredecessor");
		assertEquals(usedConditionsList.get(1).get("organisation_id"), "someId");

		assertEquals(recordReader.usedTableNames.get(2), "divaOrganisationPredecessor");
		assertEquals(usedConditionsList.get(2).get("predecessor_id"), "someId");
	}

	private void assertReadDataIsSentToConverterUsingReadListReadIndexAndConverterIndex(
			List<Map<String, String>> listToReadFrom, int readerIndex, int converterIndex) {
		DivaDbToCoraConverterSpy predecessorConverter = (DivaDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(converterIndex);

		Map<String, String> firstPredecessorRead = listToReadFrom.get(readerIndex);
		Map<String, String> mapSentToConverter = predecessorConverter.mapToConvert;
		assertEquals(firstPredecessorRead, mapSentToConverter);
	}

	@Test
	public void testReadOrganisationCanHandleNullPredecessorsAndSuccessors() throws Exception {
		recordReaderFactory.numOfPredecessorsToReturn = -1;
		DataGroup convertedOrganisation = toCoraOrganisation.readAndConvertOneRow(TABLE_NAME,
				"someId");
		RecordReaderSpy recordReader = recordReaderFactory.factored;

		assertCorrectTableNamesAndConditionsAreUsedWhenReading(recordReader);

		DivaDbToCoraConverterSpy organisationConverter = (DivaDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		Map<String, String> readOrganisation = recordReader.oneRowRead;
		Map<String, String> mapSentToFirstConverter = organisationConverter.mapToConvert;
		assertEquals(readOrganisation, mapSentToFirstConverter);

		assertFalse(convertedOrganisation.containsChildWithNameInData("from Db converter"));
	}

	@Test
	public void testReadOrganisationCallsDatabaseAndReturnsConvertedResultWithOnePredecessor()
			throws Exception {
		recordReaderFactory.numOfPredecessorsToReturn = 1;
		DataGroup convertedOrganisation = toCoraOrganisation.readAndConvertOneRow(TABLE_NAME,
				"someId");
		RecordReaderSpy recordReader = recordReaderFactory.factored;

		assertCorrectTableNamesAndConditionsAreUsedWhenReading(recordReader);
		assertEquals(recordReader.predecessorsToReturn.size(), 1);

		assertEquals(converterFactory.factoredTypes.get(0), "divaOrganisation");
		assertEquals(converterFactory.factoredTypes.get(1), "divaOrganisationPredecessor");
		assertEquals(converterFactory.factoredTypes.size(), 2);

		DivaDbToCoraConverterSpy organisationConverter = (DivaDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		Map<String, String> readOrganisation = recordReader.oneRowRead;
		Map<String, String> mapSentToFirstConverter = organisationConverter.mapToConvert;
		assertEquals(readOrganisation, mapSentToFirstConverter);

		List<Map<String, String>> predecessorsToReturn = recordReader.predecessorsToReturn;
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
		DataGroup convertedOrganisation = toCoraOrganisation.readAndConvertOneRow(TABLE_NAME,
				"someId");
		RecordReaderSpy recordReader = recordReaderFactory.factored;

		assertEquals(recordReader.predecessorsToReturn.size(), 3);
		assertCorrectTableNamesAndConditionsAreUsedWhenReading(recordReader);

		DivaDbToCoraConverterSpy organisationConverter = (DivaDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		Map<String, String> firstReadResult = recordReader.returnedList.get(0);
		Map<String, String> mapSentToFirstConverter = organisationConverter.mapToConvert;
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
	public void testReadOrganisationCallsDatabaseAndReturnsConvertedResultWithOneSuccessor()
			throws Exception {
		recordReaderFactory.numOfSuccessorsToReturn = 1;
		DataGroup convertedOrganisation = toCoraOrganisation.readAndConvertOneRow(TABLE_NAME,
				"someId");
		RecordReaderSpy recordReader = recordReaderFactory.factored;

		assertCorrectTableNamesAndConditionsAreUsedWhenReading(recordReader);
		assertEquals(recordReader.successorsToReturn.size(), 1);

		assertEquals(converterFactory.factoredTypes.get(0), "divaOrganisation");
		assertEquals(converterFactory.factoredTypes.get(1), "divaOrganisationSuccessor");
		assertEquals(converterFactory.factoredTypes.size(), 2);

		DivaDbToCoraConverterSpy organisationConverter = (DivaDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		Map<String, String> readOrganisation = recordReader.oneRowRead;
		Map<String, String> mapSentToFirstConverter = organisationConverter.mapToConvert;
		assertEquals(readOrganisation, mapSentToFirstConverter);

		List<Map<String, String>> successorsToReturn = recordReader.successorsToReturn;
		assertReadDataIsSentToConverterUsingReadListReadIndexAndConverterIndex(successorsToReturn,
				0, 1);

		assertTrue(convertedOrganisation.containsChildWithNameInData("from Db converter"));

		List<DataGroup> predecessors = convertedOrganisation
				.getAllGroupsWithNameInData("from Db converter");
		assertEquals(predecessors.get(0).getRepeatId(), "0");
		assertEquals(convertedOrganisation, organisationConverter.convertedDbDataGroup);
	}

	@Test
	public void testReadOrganisationCallsDatabaseAndReturnsConvertedResultWithManySucessors()
			throws Exception {
		recordReaderFactory.numOfSuccessorsToReturn = 3;

		DataGroup convertedOrganisation = toCoraOrganisation.readAndConvertOneRow(TABLE_NAME,
				"someId");
		RecordReaderSpy recordReader = recordReaderFactory.factored;

		assertEquals(recordReader.successorsToReturn.size(), 3);
		assertCorrectTableNamesAndConditionsAreUsedWhenReading(recordReader);

		DivaDbToCoraConverterSpy organisationConverter = (DivaDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		Map<String, String> firstReadResult = recordReader.returnedList.get(0);
		Map<String, String> mapSentToFirstConverter = organisationConverter.mapToConvert;
		assertEquals(firstReadResult, mapSentToFirstConverter);

		assertEquals(converterFactory.factoredTypes.get(0), "divaOrganisation");
		assertEquals(converterFactory.factoredTypes.get(1), "divaOrganisationSuccessor");
		assertEquals(converterFactory.factoredTypes.get(2), "divaOrganisationSuccessor");
		assertEquals(converterFactory.factoredTypes.get(3), "divaOrganisationSuccessor");
		assertEquals(converterFactory.factoredTypes.size(), 4);

		assertReadDataIsSentToConverterUsingReadListReadIndexAndConverterIndex(
				recordReader.successorsToReturn, 0, 1);
		assertReadDataIsSentToConverterUsingReadListReadIndexAndConverterIndex(
				recordReader.successorsToReturn, 1, 2);
		assertReadDataIsSentToConverterUsingReadListReadIndexAndConverterIndex(
				recordReader.successorsToReturn, 2, 3);

		assertCorrectRepeatIdInAddedChildrenUsingIndex(convertedOrganisation, 0);
		assertCorrectRepeatIdInAddedChildrenUsingIndex(convertedOrganisation, 1);
		assertCorrectRepeatIdInAddedChildrenUsingIndex(convertedOrganisation, 2);

		assertEquals(convertedOrganisation, organisationConverter.convertedDbDataGroup);
	}

	@Test
	public void testReadOrganisationConvertnedResultWithOneSucessorsWithEmptyClosedDate()
			throws Exception {
		recordReaderFactory.numOfSuccessorsToReturn = 1;

		DataGroup convertedOrganisation = toCoraOrganisation.readAndConvertOneRow(TABLE_NAME,
				"someIdWithEmptyClosedDate");

		DivaDbToCoraConverterSpy organisationConverter = (DivaDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);

		DivaDbToCoraConverterSpy firstSuccessorConverter = (DivaDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(1);
		assertFalse(firstSuccessorConverter.mapToConvert.containsKey("closed_date"));

		assertEquals(convertedOrganisation, organisationConverter.convertedDbDataGroup);
	}

	@Test
	public void testReadOrganisationConvertedResultWithManySucessorsWithClosedDate()
			throws Exception {
		recordReaderFactory.numOfSuccessorsToReturn = 3;

		DataGroup convertedOrganisation = toCoraOrganisation.readAndConvertOneRow(TABLE_NAME,
				"someIdWithClosedDate");
		RecordReaderSpy recordReader = recordReaderFactory.factored;

		assertEquals(recordReader.successorsToReturn.size(), 3);

		DivaDbToCoraConverterSpy organisationConverter = (DivaDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		Map<String, String> firstReadResult = recordReader.returnedList.get(0);
		Map<String, String> mapSentToFirstConverter = organisationConverter.mapToConvert;
		assertEquals(firstReadResult, mapSentToFirstConverter);

		assertClosedDateIsSentToSuccesorConverterWithIndex(1);
		assertClosedDateIsSentToSuccesorConverterWithIndex(2);
		assertClosedDateIsSentToSuccesorConverterWithIndex(3);

		assertEquals(convertedOrganisation, organisationConverter.convertedDbDataGroup);
	}

	private void assertClosedDateIsSentToSuccesorConverterWithIndex(int index) {
		DivaDbToCoraConverterSpy firstSuccessorConverter = (DivaDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(index);
		assertTrue(firstSuccessorConverter.mapToConvert.containsKey("closed_date"));
	}
}
