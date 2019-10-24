/*
 * Copyright 2018, 2019 Uppsala University Library
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.StorageReadResult;

public class DivaDbToCoraRecordStorageTest {
	private static final String TABLE_NAME = "divaOrganisation";
	private DivaDbToCoraRecordStorage divaToCoraRecordStorage;
	private DivaDbToCoraConverterFactorySpy converterFactory;
	private RecordReaderFactorySpy recordReaderFactory;
	private DivaDbToCoraFactorySpy divaDbToCoraFactory;
	private RecordUpdaterFactorySpy recordUpdaterFactory;

	@BeforeMethod
	public void BeforeMethod() {
		converterFactory = new DivaDbToCoraConverterFactorySpy();
		recordReaderFactory = new RecordReaderFactorySpy();
		recordUpdaterFactory = new RecordUpdaterFactorySpy();
		divaDbToCoraFactory = new DivaDbToCoraFactorySpy();

		divaToCoraRecordStorage = DivaDbToCoraRecordStorage
				.usingRecordReaderFactoryAndRecordUpdaterFactoryConverterFactoryAndDbToCoraFactory(
						recordReaderFactory, converterFactory, divaDbToCoraFactory,
						recordUpdaterFactory);
	}

	@Test
	public void testInit() throws Exception {
		assertNotNull(divaToCoraRecordStorage);
	}

	@Test
	public void divaToCoraRecordStorageImplementsRecordStorage() throws Exception {
		assertTrue(divaToCoraRecordStorage instanceof RecordStorage);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "read is not implemented for type: null")
	public void readThrowsNotImplementedException() throws Exception {
		divaToCoraRecordStorage.read(null, null);
	}

	@Test
	public void testCallToDivaDbToCoraFactory() throws Exception {
		divaToCoraRecordStorage.read(TABLE_NAME, "someId");
		assertTrue(divaDbToCoraFactory.factorWasCalled);
		assertEquals(divaDbToCoraFactory.type, "divaOrganisation");
	}

	@Test
	public void testReadOrganisationMakeCorrectCalls() throws Exception {
		divaToCoraRecordStorage.read(TABLE_NAME, "someId");
		DivaDbToCoraSpy factored = divaDbToCoraFactory.factored;
		assertEquals(factored.type, TABLE_NAME);
		assertEquals(factored.id, "someId");
	}

	@Test
	public void testOrganisationFromDivaDbToCoraIsReturnedFromRead() throws Exception {
		DataGroup readOrganisation = divaToCoraRecordStorage.read(TABLE_NAME, "someId");
		DivaDbToCoraSpy factored = divaDbToCoraFactory.factored;
		assertEquals(readOrganisation, factored.dataGroup);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "create is not implemented")
	public void createThrowsNotImplementedException() throws Exception {
		divaToCoraRecordStorage.create(null, null, null, null, null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "deleteByTypeAndId is not implemented")
	public void deleteByTypeAndIdThrowsNotImplementedException() throws Exception {
		divaToCoraRecordStorage.deleteByTypeAndId(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "linksExistForRecord is not implemented")
	public void linksExistForRecordThrowsNotImplementedException() throws Exception {
		divaToCoraRecordStorage.linksExistForRecord(null, null);
	}

	@Test
	public void testUpdateOrganisationFactorDbReader() throws Exception {
		String type = "divaOrganisation";
		String id = "";
		DataGroup record = DataGroup.withNameInData("organisation");
		String dataDivider = "";
		divaToCoraRecordStorage.update(type, id, record, null, null, dataDivider);
		assertTrue(recordUpdaterFactory.factorWasCalled);

	}

	@Test
	public void testUpdateOrganisationCorrectDbReaderIsFactored() throws Exception {
		String type = "divaOrganisation";
		String id = "";
		DataGroup record = DataGroup.withNameInData("organisation");
		String dataDivider = "";
		divaToCoraRecordStorage.update(type, id, record, null, null, dataDivider);

		RecordUpdaterSpy factoredUpdater = recordUpdaterFactory.factoredUpdater;
		assertEquals(factoredUpdater.tableName, "organisation");

	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "update is not implemented")
	public void updateThrowsNotImplementedException() throws Exception {
		divaToCoraRecordStorage.update(null, null, null, null, null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "readList is not implemented for type: null")
	public void readListThrowsNotImplementedException() throws Exception {
		divaToCoraRecordStorage.readList(null, null);
	}

	@Test
	public void testReadOrganisationListFactorDbReader() throws Exception {
		divaToCoraRecordStorage.readList(TABLE_NAME, DataGroup.withNameInData("filter"));
		assertTrue(recordReaderFactory.factorWasCalled);
	}

	@Test
	public void testReadOrganisationListCountryTableRequestedFromReader() throws Exception {
		divaToCoraRecordStorage.readList(TABLE_NAME, DataGroup.withNameInData("filter"));
		RecordReaderSpy recordReader = recordReaderFactory.factored;
		assertEquals(recordReader.usedTableName, TABLE_NAME);
	}

	@Test
	public void testReadOrganisationListConverterIsFactored() throws Exception {
		divaToCoraRecordStorage.readList(TABLE_NAME, DataGroup.withNameInData("filter"));
		DivaDbToCoraConverter divaDbToCoraConverter = converterFactory.factoredConverters.get(0);
		assertNotNull(divaDbToCoraConverter);
	}

	@Test
	public void testReadOrganisationListConverterIsCalledWithDataFromDbStorage() throws Exception {
		divaToCoraRecordStorage.readList(TABLE_NAME, DataGroup.withNameInData("filter"));
		RecordReaderSpy recordReader = recordReaderFactory.factored;
		DivaDbToCoraConverterSpy divaDbToCoraConverter = (DivaDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		assertNotNull(divaDbToCoraConverter.mapToConvert);
		assertEquals(recordReader.returnedList.get(0), divaDbToCoraConverter.mapToConvert);
	}

	@Test
	public void testReadOrganisationListConverteredIsAddedToList() throws Exception {
		StorageReadResult spiderReadresult = divaToCoraRecordStorage.readList(TABLE_NAME,
				DataGroup.withNameInData("filter"));
		List<DataGroup> readCountryList = spiderReadresult.listOfDataGroups;
		RecordReaderSpy recordReader = recordReaderFactory.factored;
		DivaDbToCoraConverterSpy divaDbToCoraConverter = (DivaDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		assertEquals(recordReader.returnedList.size(), 1);
		assertEquals(recordReader.returnedList.get(0), divaDbToCoraConverter.mapToConvert);
		assertEquals(readCountryList.get(0), divaDbToCoraConverter.convertedDbDataGroup);
	}

	@Test
	public void testReadOrganisationListConverteredMoreThanOneIsAddedToList() throws Exception {
		recordReaderFactory.noOfRecordsToReturn = 3;
		StorageReadResult storageReadResult = divaToCoraRecordStorage.readList(TABLE_NAME,
				DataGroup.withNameInData("filter"));
		List<DataGroup> readOrganisationList = storageReadResult.listOfDataGroups;
		RecordReaderSpy recordReader = recordReaderFactory.factored;

		assertEquals(recordReader.returnedList.size(), 3);

		DivaDbToCoraConverterSpy divaDbToCoraConverter = (DivaDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(0);
		assertEquals(recordReader.returnedList.get(0), divaDbToCoraConverter.mapToConvert);
		assertEquals(readOrganisationList.get(0), divaDbToCoraConverter.convertedDbDataGroup);

		DivaDbToCoraConverterSpy divaDbToCoraConverter2 = (DivaDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(1);
		assertEquals(recordReader.returnedList.get(1), divaDbToCoraConverter2.mapToConvert);
		assertEquals(readOrganisationList.get(1), divaDbToCoraConverter2.convertedDbDataGroup);

		DivaDbToCoraConverterSpy divaDbToCoraConverter3 = (DivaDbToCoraConverterSpy) converterFactory.factoredConverters
				.get(2);
		assertEquals(recordReader.returnedList.get(2), divaDbToCoraConverter3.mapToConvert);
		assertEquals(readOrganisationList.get(2), divaDbToCoraConverter3.convertedDbDataGroup);

	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "readAbstractList is not implemented")
	public void readAbstractListThrowsNotImplementedException() throws Exception {
		divaToCoraRecordStorage.readAbstractList(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "readLinkList is not implemented")
	public void readLinkListThrowsNotImplementedException() throws Exception {
		divaToCoraRecordStorage.readLinkList(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "generateLinkCollectionPointingToRecord is not implemented")
	public void generateLinkCollectionPointingToRecordThrowsNotImplementedException()
			throws Exception {
		divaToCoraRecordStorage.generateLinkCollectionPointingToRecord(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "recordsExistForRecordType is not implemented")
	public void recordsExistForRecordTypeThrowsNotImplementedException() throws Exception {
		divaToCoraRecordStorage.recordsExistForRecordType(null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "recordExistsForAbstractOrImplementingRecordTypeAndRecordId is not implemented")
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdThrowsNotImplementedException()
			throws Exception {
		divaToCoraRecordStorage.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(null,
				null);
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForDivaOrganisation() {
		boolean organisationExists = divaToCoraRecordStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId("divaOrganisation",
						"26");
		RecordReaderSpy readerSpy = recordReaderFactory.factored;
		assertEquals(readerSpy.usedTableName, "organisation");
		Map<String, Object> usedConditions = readerSpy.usedConditions;
		assertEquals(usedConditions.get("organisation_id"), 26);
		assertTrue(organisationExists);
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForDivaOrganisationWhenOrganisationDoesNotExist() {
		boolean organisationExists = divaToCoraRecordStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId("divaOrganisation",
						"600");
		RecordReaderSpy readerSpy = recordReaderFactory.factored;
		assertEquals(readerSpy.usedTableName, "organisation");
		Map<String, Object> usedConditions = readerSpy.usedConditions;
		assertEquals(usedConditions.get("organisation_id"), 600);
		assertFalse(organisationExists);
	}

	@Test
	public void testecordExistsDivaOrganisationCallsDataReaderWithStringIdReturnsFalse()
			throws Exception {
		divaToCoraRecordStorage.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(
				"divaOrganisation", "notAnInt");
	}
}
