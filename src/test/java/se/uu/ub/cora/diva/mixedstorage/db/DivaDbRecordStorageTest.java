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
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.StorageReadResult;

public class DivaDbRecordStorageTest {
	private static final String ORGANISATION_TYPE = "divaOrganisation";
	private DivaDbRecordStorage divaRecordStorage;
	private DivaDbToCoraConverterFactorySpy converterFactorySpy;
	private RecordReaderFactorySpy recordReaderFactorySpy;
	private DivaDbFactorySpy divaDbCreatorFactorySpy;
	private DivaDbUpdaterFactorySpy divaDbUpdaterFactorySpy;

	@BeforeMethod
	public void BeforeMethod() {
		converterFactorySpy = new DivaDbToCoraConverterFactorySpy();
		recordReaderFactorySpy = new RecordReaderFactorySpy();
		divaDbCreatorFactorySpy = new DivaDbFactorySpy();
		divaDbUpdaterFactorySpy = new DivaDbUpdaterFactorySpy();
		divaRecordStorage = DivaDbRecordStorage
				.usingRecordReaderFactoryConverterFactoryDivaFactoryAndDivaDbUpdaterFactory(
						recordReaderFactorySpy, converterFactorySpy, divaDbCreatorFactorySpy,
						divaDbUpdaterFactorySpy);
	}

	@Test
	public void testInit() throws Exception {
		assertNotNull(divaRecordStorage);
	}

	@Test
	public void divaToCoraRecordStorageImplementsRecordStorage() throws Exception {
		assertTrue(divaRecordStorage instanceof RecordStorage);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "read is not implemented for type: null")
	public void readThrowsNotImplementedException() throws Exception {
		divaRecordStorage.read(null, null);
	}

	@Test
	public void testCallToDivaDbToCoraFactory() throws Exception {
		divaRecordStorage.read(ORGANISATION_TYPE, "someId");
		assertTrue(divaDbCreatorFactorySpy.factorWasCalled);
		assertEquals(divaDbCreatorFactorySpy.type, "divaOrganisation");
	}

	@Test
	public void testReadOrganisationMakeCorrectCalls() throws Exception {
		divaRecordStorage.read(ORGANISATION_TYPE, "someId");
		DivaDbSpy factored = divaDbCreatorFactorySpy.factored;
		assertEquals(factored.type, ORGANISATION_TYPE);
		assertEquals(factored.id, "someId");
	}

	@Test
	public void testOrganisationFromDivaDbToCoraIsReturnedFromRead() throws Exception {
		DataGroup readOrganisation = divaRecordStorage.read(ORGANISATION_TYPE, "someId");
		DivaDbSpy factored = divaDbCreatorFactorySpy.factored;
		assertEquals(readOrganisation, factored.dataGroup);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "create is not implemented")
	public void createThrowsNotImplementedException() throws Exception {
		divaRecordStorage.create(null, null, null, null, null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "deleteByTypeAndId is not implemented")
	public void deleteByTypeAndIdThrowsNotImplementedException() throws Exception {
		divaRecordStorage.deleteByTypeAndId(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "linksExistForRecord is not implemented")
	public void linksExistForRecordThrowsNotImplementedException() throws Exception {
		divaRecordStorage.linksExistForRecord(null, null);
	}

	// @Test
	// public void testUpdateOrganisationFactorOrganisationTranslater() throws Exception {
	// DataGroup record = new DataGroupSpy("organisation");
	// record.addChild(new DataAtomicSpy("organisationName", "someChangedName"));
	//
	// String dataDivider = "";
	// divaToCoraRecordStorage.update("divaOrganisation", "56", record, null, null, dataDivider);
	// assertTrue(dataToDbTranslaterFactory.factorWasCalled);
	//
	// }

	@Test
	public void testUpdateOrganisationFactorOrganisationDbRecordStorageForOneType()
			throws Exception {
		DataGroup record = new DataGroupSpy("organisation");
		record.addChild(new DataAtomicSpy("organisationName", "someChangedName"));

		String dataDivider = "";
		divaRecordStorage.update("divaOrganisation", "56", record, null, null, dataDivider);
		assertTrue(divaDbUpdaterFactorySpy.factorWasCalled);
		assertEquals(divaDbUpdaterFactorySpy.types.get(0), "divaOrganisation");

	}

	// @Test
	// public void testUpdateOrganisationFactorDbUpdater() throws Exception {
	// DataGroup record = new DataGroupSpy("organisation");
	// record.addChild(new DataAtomicSpy("organisationName", "someChangedName"));
	//
	// String dataDivider = "";
	// divaToCoraRecordStorage.update("divaOrganisation", "56", record, null, null, dataDivider);
	// assertTrue(recordUpdaterFactory.factorWasCalled);
	//
	// }

	// @Test
	// public void testUpdateOrganisationUsesTranslaterFromFactory() throws Exception {
	// DataGroup organisation = new DataGroupSpy("organisation");
	// organisation.addChild(new DataAtomicSpy("organisationName", "someChangedName"));
	//
	// String dataDivider = "";
	// divaToCoraRecordStorage.update("divaOrganisation", "56", organisation, null, null,
	// dataDivider);
	//
	// DataToDbTranslaterSpy toDbTranslater = dataToDbTranslaterFactory.factoredTranslater;
	// assertEquals(toDbTranslater.dataGroup, organisation);
	//
	// RecordUpdaterSpy factoredUpdater = recordUpdaterFactory.factoredUpdater;
	// assertSame(factoredUpdater.conditions, toDbTranslater.conditions);
	// assertSame(factoredUpdater.values, toDbTranslater.values);
	// assertEquals(factoredUpdater.tableName, "organisation");
	// }
	@Test
	public void testUpdateOrganisationUsesRecordStorageForOneTypeFromFactory() throws Exception {
		DataGroup organisation = new DataGroupSpy("organisation");
		organisation.addChild(new DataAtomicSpy("organisationName", "someChangedName"));

		String dataDivider = "";
		divaRecordStorage.update("divaOrganisation", "56", organisation, null, null, dataDivider);

		DivaDbUpdaterSpy recordStorageForOneTypeSpy = (DivaDbUpdaterSpy) divaDbUpdaterFactorySpy.divaDbUpdaterList
				.get(0);
		assertEquals(recordStorageForOneTypeSpy.dataGroup, organisation);
	}

	// @Test
	// public void testUpdateOrganisationUsesOrganisationAlternativeName() throws Exception {
	// // TODO: kolla att OrganisationAlternativeName används
	// DataGroup record = new DataGroupSpy("organisation");
	// record.addChild(new DataAtomicSpy("organisationName", "someChangedName"));
	//
	// String dataDivider = "";
	// divaToCoraRecordStorage.update("divaOrganisation", "56", record, null, null, dataDivider);
	// assertTrue(recordUpdaterFactory.factorWasCalled);
	//
	// }

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "update is not implemented")
	public void updateThrowsNotImplementedException() throws Exception {
		divaRecordStorage.update(null, null, null, null, null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "readList is not implemented for type: null")
	public void readListThrowsNotImplementedException() throws Exception {
		divaRecordStorage.readList(null, null);
	}

	@Test
	public void testReadOrganisationListFactorDbReader() throws Exception {
		divaRecordStorage.readList(ORGANISATION_TYPE, new DataGroupSpy("filter"));
		assertTrue(recordReaderFactorySpy.factorWasCalled);
	}

	@Test
	public void testReadOrganisationListCountryTableRequestedFromReader() throws Exception {
		divaRecordStorage.readList(ORGANISATION_TYPE, new DataGroupSpy("filter"));
		RecordReaderSpy recordReader = recordReaderFactorySpy.factored;
		assertEquals(recordReader.usedTableName, ORGANISATION_TYPE);
	}

	@Test
	public void testReadOrganisationListConverterIsFactored() throws Exception {
		divaRecordStorage.readList(ORGANISATION_TYPE, new DataGroupSpy("filter"));
		DivaDbToCoraConverter divaDbToCoraConverter = converterFactorySpy.factoredConverters.get(0);
		assertNotNull(divaDbToCoraConverter);
	}

	@Test
	public void testReadOrganisationListConverterIsCalledWithDataFromDbStorage() throws Exception {
		divaRecordStorage.readList(ORGANISATION_TYPE, new DataGroupSpy("filter"));
		RecordReaderSpy recordReader = recordReaderFactorySpy.factored;
		DivaDbToCoraConverterSpy divaDbToCoraConverter = (DivaDbToCoraConverterSpy) converterFactorySpy.factoredConverters
				.get(0);
		assertNotNull(divaDbToCoraConverter.mapToConvert);
		assertEquals(recordReader.returnedList.get(0), divaDbToCoraConverter.mapToConvert);
	}

	@Test
	public void testReadOrganisationListConverteredIsAddedToList() throws Exception {
		StorageReadResult spiderReadresult = divaRecordStorage.readList(ORGANISATION_TYPE,
				new DataGroupSpy("filter"));
		List<DataGroup> readCountryList = spiderReadresult.listOfDataGroups;
		RecordReaderSpy recordReader = recordReaderFactorySpy.factored;
		DivaDbToCoraConverterSpy divaDbToCoraConverter = (DivaDbToCoraConverterSpy) converterFactorySpy.factoredConverters
				.get(0);
		assertEquals(recordReader.returnedList.size(), 1);
		assertEquals(recordReader.returnedList.get(0), divaDbToCoraConverter.mapToConvert);
		assertEquals(readCountryList.get(0), divaDbToCoraConverter.convertedDbDataGroup);
	}

	@Test
	public void testReadOrganisationListConverteredMoreThanOneIsAddedToList() throws Exception {
		recordReaderFactorySpy.noOfRecordsToReturn = 3;
		StorageReadResult storageReadResult = divaRecordStorage.readList(ORGANISATION_TYPE,
				new DataGroupSpy("filter"));
		List<DataGroup> readOrganisationList = storageReadResult.listOfDataGroups;
		RecordReaderSpy recordReader = recordReaderFactorySpy.factored;

		assertEquals(recordReader.returnedList.size(), 3);

		DivaDbToCoraConverterSpy divaDbToCoraConverter = (DivaDbToCoraConverterSpy) converterFactorySpy.factoredConverters
				.get(0);
		assertEquals(recordReader.returnedList.get(0), divaDbToCoraConverter.mapToConvert);
		assertEquals(readOrganisationList.get(0), divaDbToCoraConverter.convertedDbDataGroup);

		DivaDbToCoraConverterSpy divaDbToCoraConverter2 = (DivaDbToCoraConverterSpy) converterFactorySpy.factoredConverters
				.get(1);
		assertEquals(recordReader.returnedList.get(1), divaDbToCoraConverter2.mapToConvert);
		assertEquals(readOrganisationList.get(1), divaDbToCoraConverter2.convertedDbDataGroup);

		DivaDbToCoraConverterSpy divaDbToCoraConverter3 = (DivaDbToCoraConverterSpy) converterFactorySpy.factoredConverters
				.get(2);
		assertEquals(recordReader.returnedList.get(2), divaDbToCoraConverter3.mapToConvert);
		assertEquals(readOrganisationList.get(2), divaDbToCoraConverter3.convertedDbDataGroup);

	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "readAbstractList is not implemented")
	public void readAbstractListThrowsNotImplementedException() throws Exception {
		divaRecordStorage.readAbstractList(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "readLinkList is not implemented")
	public void readLinkListThrowsNotImplementedException() throws Exception {
		divaRecordStorage.readLinkList(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "generateLinkCollectionPointingToRecord is not implemented")
	public void generateLinkCollectionPointingToRecordThrowsNotImplementedException()
			throws Exception {
		divaRecordStorage.generateLinkCollectionPointingToRecord(null, null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "recordsExistForRecordType is not implemented")
	public void recordsExistForRecordTypeThrowsNotImplementedException() throws Exception {
		divaRecordStorage.recordsExistForRecordType(null);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "recordExistsForAbstractOrImplementingRecordTypeAndRecordId is not implemented")
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdThrowsNotImplementedException()
			throws Exception {
		divaRecordStorage.recordExistsForAbstractOrImplementingRecordTypeAndRecordId(null, null);
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForDivaOrganisation() {
		boolean organisationExists = divaRecordStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId("divaOrganisation",
						"26");
		RecordReaderSpy readerSpy = recordReaderFactorySpy.factored;
		assertEquals(readerSpy.usedTableName, "organisation");
		Map<String, Object> usedConditions = readerSpy.usedConditions;
		assertEquals(usedConditions.get("organisation_id"), 26);
		assertTrue(organisationExists);
	}

	@Test
	public void recordExistsForAbstractOrImplementingRecordTypeAndRecordIdForDivaOrganisationWhenOrganisationDoesNotExist() {
		boolean organisationExists = divaRecordStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId("divaOrganisation",
						"600");
		RecordReaderSpy readerSpy = recordReaderFactorySpy.factored;
		assertEquals(readerSpy.usedTableName, "organisation");
		Map<String, Object> usedConditions = readerSpy.usedConditions;
		assertEquals(usedConditions.get("organisation_id"), 600);
		assertFalse(organisationExists);
	}

	@Test
	public void testRecordExistsDivaOrganisationCallsDataReaderWithStringIdReturnsFalse()
			throws Exception {
		boolean organisationExists = divaRecordStorage
				.recordExistsForAbstractOrImplementingRecordTypeAndRecordId("divaOrganisation",
						"notAnInt");
		assertFalse(organisationExists);
	}
}