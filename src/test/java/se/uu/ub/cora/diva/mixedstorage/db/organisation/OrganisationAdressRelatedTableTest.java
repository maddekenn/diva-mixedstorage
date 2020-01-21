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
import se.uu.ub.cora.diva.mixedstorage.db.RecordUpdaterFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.RecordUpdaterSpy;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTable;

public class OrganisationAdressRelatedTableTest {

	private RecordReaderRelatedTableFactorySpy recordReaderFactory;
	private RecordDeleterSpy recordDeleter;
	private RecordCreatorSpy recordCreator;
	private RecordUpdaterFactorySpy recordUpdaterFactory;
	private RelatedTable address;

	@BeforeMethod
	public void setUp() {
		recordReaderFactory = new RecordReaderRelatedTableFactorySpy();
		recordDeleter = new RecordDeleterSpy();
		recordCreator = new RecordCreatorSpy();
		recordUpdaterFactory = new RecordUpdaterFactorySpy();
		address = new OrganisationAdressRelatedTable(recordReaderFactory, recordDeleter,
				recordCreator, recordUpdaterFactory);

	}

	@Test
	public void testNoAddressInDataGroupNoInAddressDatabase() {
		DataGroup organisation = createDataGroupWithId("678");
		int organisationId = 678;
		addOrganisationToReturnFromSpy("organisation", organisationId, -1);
		address.handleDbForDataGroup(organisation);
		assertFirstReadRowIsOrganisation(organisationId);
		assertEquals(recordReaderFactory.factoredReaders.size(), 1);
	}

	private void assertFirstReadRowIsOrganisation(int organisationId) {
		RecordReaderAddressSpy organisationReader = recordReaderFactory.factoredReaders.get(0);
		assertEquals(organisationReader.usedTableNames.get(0), "organisation");
		assertEquals(recordReaderFactory.factoredReaders.get(0).usedConditions.get(0)
				.get("organisation_id"), organisationId);
	}

	private DataGroup createDataGroupWithId(String id) {
		DataGroup dataGroup = new DataGroupSpy("organisation");
		DataGroupSpy recordInfo = new DataGroupSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", id));
		dataGroup.addChild(recordInfo);
		return dataGroup;
	}

	private void addOrganisationToReturnFromSpy(String tableName, int organisationId,
			int addressId) {
		Map<String, Object> rowToReturn = createMapInSpyForTableName(tableName);
		if (addressId > 0) {
			rowToReturn.put("address_id", addressId);
		}
		rowToReturn.put("organisation_id", organisationId);
	}

	private Map<String, Object> createMapInSpyForTableName(String tableName) {
		List<Map<String, Object>> rowsInSpy = new ArrayList<>();
		if (recordReaderFactory.rowsToReturn.containsKey(tableName)) {
			rowsInSpy = recordReaderFactory.rowsToReturn.get(tableName);
		} else {
			recordReaderFactory.rowsToReturn.put(tableName, rowsInSpy);
		}

		Map<String, Object> rowToReturn = new HashMap<>();
		rowsInSpy.add(rowToReturn);
		return rowToReturn;
	}

	@Test
	public void testNoAddressInDataGroupButAddressInDatabase() {
		int organisationId = 678;
		DataGroup organisation = createDataGroupWithId("678");
		addOrganisationToReturnFromSpy("organisation", organisationId, 4);
		addOrganisationAddressToReturnFromSpy("organisation_address", 4);

		address.handleDbForDataGroup(organisation);

		assertFirstReadRowIsOrganisation(organisationId);
		assertEquals(recordReaderFactory.factoredReaders.size(), 1);

		assertTrue(recordDeleter.deleteWasCalled);
		assertEquals(recordDeleter.usedTableName, "organisation_address");

		assertConditionsForDeleteContainsIdFromRead();

		RecordUpdaterSpy factoredUpdater = recordUpdaterFactory.factoredUpdater;
		assertTrue(factoredUpdater.updateWasCalled);
		assertEquals(factoredUpdater.tableName, "organisation");
		assertTrue(factoredUpdater.values.containsKey("address_id"));
		assertEquals(factoredUpdater.values.get("address_id"), null);
		assertEquals(factoredUpdater.conditions.get("organisation_id"), 678);

	}

	private void assertConditionsForDeleteContainsIdFromRead() {
		Map<String, Object> organisationRow = recordReaderFactory.factoredReaders
				.get(0).rowsToReturn.get("organisation").get(0);

		int addressIdReturnedFromOrganisationReadSpy = (int) organisationRow.get("address_id");
		assertEquals(recordDeleter.usedConditions.get("address_id"),
				addressIdReturnedFromOrganisationReadSpy);
	}

	private void addOrganisationAddressToReturnFromSpy(String tableName, int addressId) {
		Map<String, Object> rowToReturn = createMapInSpyForTableName(tableName);
		rowToReturn.put("address_id", addressId);
	}

	@Test
	public void testCityInDataGroupAddressInDatabase() {
		int organisationId = 678;
		DataGroup organisation = createDataGroupWithId("678");
		organisation.addChild(new DataAtomicSpy("city", "City of rock and roll"));

		addOrganisationToReturnFromSpy("organisation", organisationId, 4);
		addOrganisationAddressToReturnFromSpy("organisation_address", 4);

		address.handleDbForDataGroup(organisation);

		assertFirstReadRowIsOrganisation(organisationId);
		assertEquals(recordReaderFactory.factoredReaders.size(), 2);

		assertFalse(recordDeleter.deleteWasCalled);
		// assertEquals(recordDeleter.usedTableName, "organisation_address");
		//
		// assertConditionsForDeleteContainsIdFromRead();
		//
		RecordUpdaterSpy factoredUpdater = recordUpdaterFactory.factoredUpdater;
		assertTrue(factoredUpdater.updateWasCalled);
		// assertEquals(factoredUpdater.tableName, "organisation");
		// assertTrue(factoredUpdater.values.containsKey("address_id"));
		// assertEquals(factoredUpdater.values.get("address_id"), null);
		// assertEquals(factoredUpdater.conditions.get("organisation_id"), 678);

	}

	// @Test
	// public void testOneNameInDbSameNameInDataGroup() {
	// DataGroup organisation = createDataGroupWithId("678");
	// addAlternativeName(organisation, "some english name");
	//
	// addNameToReturnFromSpy("organisation_name", 234, 678);
	//
	// adress.handleDbForDataGroup(organisation);
	// assertCorrectDataSentToRecordReader();
	// assertFalse(recordDeleter.deleteWasCalled);
	//
	// assertFalse(recordCreator.insertWasCalled);
	//
	// }
	//
	// @Test
	// public void testOneNameInDbDifferentNameInDataGroup() {
	// DataGroup organisation = createDataGroupWithId("678");
	// addAlternativeName(organisation, "some other english name");
	//
	// addNameToReturnFromSpy("organisation_name", 234, 678);
	//
	// adress.handleDbForDataGroup(organisation);
	// assertCorrectDataSentToRecordReader();
	//
	// assertTrue(recordDeleter.deleteWasCalled);
	//
	// assertTrue(recordCreator.insertWasCalled);
	// assertCorrectValuesSentToInsert("some other english name");
	//
	// }
	//
	// private void assertCorrectValuesSentToInsert(String name) {
	// assertEquals(recordReader.sequenceName, "name_sequence");
	// assertEquals(recordCreator.usedTableName, "organisation_name");
	// assertEquals(recordCreator.values.get("organisation_name_id"),
	// recordReader.nextVal.get("nextval"));
	// assertEquals(recordCreator.values.get("locale"), "en");
	// assertEquals(recordCreator.values.get("organisation_id"), 678);
	//
	// Timestamp lastUpdated = (Timestamp) recordCreator.values.get("last_updated");
	// String lastUpdatedString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
	// .format(lastUpdated);
	//
	// assertTrue(lastUpdatedString
	// .matches("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\.\\d{1,3}"));
	//
	// assertEquals(recordCreator.values.get("organisation_name"), name);
	// }
	//
	// private void addAlternativeName(DataGroup organisation, String name) {
	// DataGroupSpy alternativeNameGroup = new DataGroupSpy("alternativeName");
	// alternativeNameGroup.addChild(new DataAtomicSpy("organisationName", name));
	// alternativeNameGroup.addChild(new DataAtomicSpy("language", "en"));
	// organisation.addChild(alternativeNameGroup);
	// }
	//
	// @Test
	// public void testNoNameInDbButNameInDataGroup() {
	// DataGroup organisation = createDataGroupWithId("678");
	// DataGroupSpy alternativeNameGroup = new DataGroupSpy("alternativeName");
	// alternativeNameGroup.addChild(new DataAtomicSpy("organisationName", "some english name"));
	// alternativeNameGroup.addChild(new DataAtomicSpy("language", "en"));
	// organisation.addChild(alternativeNameGroup);
	//
	// adress.handleDbForDataGroup(organisation);
	// assertCorrectDataSentToRecordReader();
	// assertFalse(recordDeleter.deleteWasCalled);
	//
	// assertTrue(recordCreator.insertWasCalled);
	// assertCorrectValuesSentToInsert("some english name");
	// }
}
