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
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

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
	private DivaDbFactorySpy divaDbFactorySpy;
	private DivaDbUpdaterFactorySpy divaDbUpdaterFactorySpy;

	@BeforeMethod
	public void BeforeMethod() {
		converterFactorySpy = new DivaDbToCoraConverterFactorySpy();
		recordReaderFactorySpy = new RecordReaderFactorySpy();
		divaDbFactorySpy = new DivaDbFactorySpy();
		divaDbUpdaterFactorySpy = new DivaDbUpdaterFactorySpy();
		divaRecordStorage = DivaDbRecordStorage
				.usingRecordReaderFactoryDivaFactoryAndDivaDbUpdaterFactory(recordReaderFactorySpy,
						divaDbFactorySpy, divaDbUpdaterFactorySpy);
	}

	@Test
	public void testInit() throws Exception {
		assertNotNull(divaRecordStorage);
	}

	@Test
	public void divaToCoraRecordStorageImplementsRecordStorage() throws Exception {
		assertTrue(divaRecordStorage instanceof RecordStorage);
	}

	@Test
	public void testCallToDivaDbToCoraFactory() throws Exception {
		divaRecordStorage.read(ORGANISATION_TYPE, "someId");
		assertTrue(divaDbFactorySpy.factorWasCalled);
		assertEquals(divaDbFactorySpy.type, "divaOrganisation");
	}

	@Test
	public void testReadOrganisationMakeCorrectCalls() throws Exception {
		divaRecordStorage.read(ORGANISATION_TYPE, "someId");
		DivaDbSpy factored = divaDbFactorySpy.factored;
		assertEquals(factored.type, ORGANISATION_TYPE);
		assertEquals(factored.id, "someId");
	}

	@Test
	public void testOrganisationFromDivaDbToCoraIsReturnedFromRead() throws Exception {
		DataGroup readOrganisation = divaRecordStorage.read(ORGANISATION_TYPE, "someId");
		DivaDbSpy factored = divaDbFactorySpy.factored;
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
	public void testReadOrganisationListAllIdsAreReadFromDb() throws Exception {
		RecordReaderFactoryForListSpy recordReaderFactoryForList = new RecordReaderFactoryForListSpy();
		divaRecordStorage = DivaDbRecordStorage
				.usingRecordReaderFactoryDivaFactoryAndDivaDbUpdaterFactory(
						recordReaderFactoryForList, divaDbFactorySpy, divaDbUpdaterFactorySpy);

		StorageReadResult readList = divaRecordStorage.readList(ORGANISATION_TYPE,
				new DataGroupSpy("filter"));

		RecordReaderForListSpy recordReader = recordReaderFactoryForList.factored;

		assertIdFromListIsSentToReaderUsingIndex(recordReader, 0);
		assertIdFromListIsSentToReaderUsingIndex(recordReader, 1);
		assertIdFromListIsSentToReaderUsingIndex(recordReader, 2);

		assertReadRecordIsAddedToResultUsingIndex(readList, 0);
		assertReadRecordIsAddedToResultUsingIndex(readList, 1);
		assertReadRecordIsAddedToResultUsingIndex(readList, 2);
	}

	private void assertIdFromListIsSentToReaderUsingIndex(RecordReaderForListSpy recordReader,
			int index) {
		DivaDbSpy factoredDbSpy = divaDbFactorySpy.factoredList.get(index);
		String idFromReadRecord = (String) recordReader.returnedList.get(index).get("id");
		assertEquals(idFromReadRecord, factoredDbSpy.id);
		assertEquals(divaDbFactorySpy.factoredList.size(), 3);
	}

	private void assertReadRecordIsAddedToResultUsingIndex(StorageReadResult readList, int index) {
		DivaDbSpy factoredDbSpy = divaDbFactorySpy.factoredList.get(index);
		assertSame(factoredDbSpy.dataGroup, readList.listOfDataGroups.get(index));
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
