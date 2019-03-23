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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.spider.data.SpiderReadResult;
import se.uu.ub.cora.spider.record.storage.RecordStorage;

public class DivaDbRecordStorageTest {
	private static final String TABLE_NAME = "divaOrganisation";
	private DivaDbRecordStorage divaToCoraRecordStorage;
	private DivaDbToCoraConverterFactorySpy toCoraConverterFactory;
	private RecordReaderFactorySpy recordReaderFactory;
	private RecordUpdaterFactorySpy recordUpdaterFactory;
	private DivaDbToCoraFactorySpy divaDbToCoraFactory;
	private CoraToDbConverterFactorySpy toDbConverterFactory;
	// private CoraToDbFactorySpy coraToDbFactory;

	@BeforeMethod
	public void BeforeMethod() {
		// dbXXFactory som innehåller factorReader, factorUpdater, factorCreator osv??
		// istället för en factory för varje?
		toCoraConverterFactory = new DivaDbToCoraConverterFactorySpy();
		recordReaderFactory = new RecordReaderFactorySpy();
		recordUpdaterFactory = new RecordUpdaterFactorySpy();
		divaDbToCoraFactory = new DivaDbToCoraFactorySpy();
		toDbConverterFactory = new CoraToDbConverterFactorySpy();
		// coraToDbFactory = new CoraToDbFactorySpy();
		divaToCoraRecordStorage = DivaDbRecordStorage.usingFactories(recordReaderFactory,
				recordUpdaterFactory, toCoraConverterFactory, divaDbToCoraFactory,
				toDbConverterFactory);
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
	public void testUpdateOrganisationMakeCorrectCalls() throws Exception {
		DataGroup dataGroup = DataGroup.withNameInData("organisation");
		divaToCoraRecordStorage.update(TABLE_NAME, "someId", dataGroup, null, null, "diva");

		CoraToDbConverterSpy factoredConverter = toDbConverterFactory.factored;
		assertEquals(toDbConverterFactory.type, TABLE_NAME);

		assertSame(factoredConverter.dataGroup, dataGroup);

		RecordUpdaterSpy factoredUpdater = (RecordUpdaterSpy) recordUpdaterFactory.factored;
		PreparedStatementInfo psInfoFromConverter = factoredConverter.psInfo;

		assertSame(psInfoFromConverter.tableName, factoredUpdater.tableName);
		assertSame(psInfoFromConverter.values, factoredUpdater.values);
		assertSame(psInfoFromConverter.conditions, factoredUpdater.conditions);
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
		DivaDbToCoraConverter divaDbToCoraConverter = toCoraConverterFactory.factoredConverters
				.get(0);
		assertNotNull(divaDbToCoraConverter);
	}

	@Test
	public void testReadOrganisationListConverterIsCalledWithDataFromDbStorage() throws Exception {
		divaToCoraRecordStorage.readList(TABLE_NAME, DataGroup.withNameInData("filter"));
		RecordReaderSpy recordReader = recordReaderFactory.factored;
		DivaDbToCoraConverterSpy divaDbToCoraConverter = (DivaDbToCoraConverterSpy) toCoraConverterFactory.factoredConverters
				.get(0);
		assertNotNull(divaDbToCoraConverter.mapToConvert);
		assertEquals(recordReader.returnedList.get(0), divaDbToCoraConverter.mapToConvert);
	}

	@Test
	public void testReadOrganisationListConverteredIsAddedToList() throws Exception {
		SpiderReadResult spiderReadresult = divaToCoraRecordStorage.readList(TABLE_NAME,
				DataGroup.withNameInData("filter"));
		List<DataGroup> readCountryList = spiderReadresult.listOfDataGroups;
		RecordReaderSpy recordReader = recordReaderFactory.factored;
		DivaDbToCoraConverterSpy divaDbToCoraConverter = (DivaDbToCoraConverterSpy) toCoraConverterFactory.factoredConverters
				.get(0);
		assertEquals(recordReader.returnedList.size(), 1);
		assertEquals(recordReader.returnedList.get(0), divaDbToCoraConverter.mapToConvert);
		assertEquals(readCountryList.get(0), divaDbToCoraConverter.convertedDbDataGroup);
	}

	@Test
	public void testReadOrganisationListConverteredMoreThanOneIsAddedToList() throws Exception {
		recordReaderFactory.noOfRecordsToReturn = 3;
		SpiderReadResult spiderReadResult = divaToCoraRecordStorage.readList(TABLE_NAME,
				DataGroup.withNameInData("filter"));
		List<DataGroup> readOrganisationList = spiderReadResult.listOfDataGroups;
		RecordReaderSpy recordReader = recordReaderFactory.factored;

		assertEquals(recordReader.returnedList.size(), 3);

		DivaDbToCoraConverterSpy divaDbToCoraConverter = (DivaDbToCoraConverterSpy) toCoraConverterFactory.factoredConverters
				.get(0);
		assertEquals(recordReader.returnedList.get(0), divaDbToCoraConverter.mapToConvert);
		assertEquals(readOrganisationList.get(0), divaDbToCoraConverter.convertedDbDataGroup);

		DivaDbToCoraConverterSpy divaDbToCoraConverter2 = (DivaDbToCoraConverterSpy) toCoraConverterFactory.factoredConverters
				.get(1);
		assertEquals(recordReader.returnedList.get(1), divaDbToCoraConverter2.mapToConvert);
		assertEquals(readOrganisationList.get(1), divaDbToCoraConverter2.convertedDbDataGroup);

		DivaDbToCoraConverterSpy divaDbToCoraConverter3 = (DivaDbToCoraConverterSpy) toCoraConverterFactory.factoredConverters
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
}
