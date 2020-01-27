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
import static org.testng.Assert.assertNull;
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
import se.uu.ub.cora.diva.mixedstorage.db.RecordUpdaterFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.RecordUpdaterSpy;

public class OrganisationAddressTableTest {

	private RecordReaderRelatedTableFactorySpy recordReaderFactory;
	private RecordDeleterSpy recordDeleter;
	private RecordCreatorSpy recordCreator;
	private RecordUpdaterFactorySpy recordUpdaterFactory;
	private ReferenceTable address;

	@BeforeMethod
	public void setUp() {
		recordReaderFactory = new RecordReaderRelatedTableFactorySpy();
		recordUpdaterFactory = new RecordUpdaterFactorySpy();
		recordDeleter = new RecordDeleterSpy();
		recordCreator = new RecordCreatorSpy();
		address = new OrganisationAddressTable(recordCreator, recordReaderFactory,
				recordUpdaterFactory, recordDeleter);

	}

	@Test
	public void testNoAddressInDataGroupNoInAddressDatabase() {
		DataGroup organisation = createDataGroupWithId("678");
		int organisationId = 678;
		addOrganisationToReturnFromSpy("organisation", organisationId, -1);
		address.handleDbForDataGroup(organisation);
		assertFirstReadRowIsOrganisation(organisationId);
		assertEquals(recordReaderFactory.factoredReaders.size(), 1);
		assertNull(recordUpdaterFactory.factoredUpdater);
		assertFalse(recordDeleter.deleteWasCalled);
		assertFalse(recordCreator.createWasCalled);

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
	public void testCityInDataGroupAndAddressInDatabase() {
		int organisationId = 678;
		DataGroup organisation = createDataGroupWithId("678");
		organisation.addChild(new DataAtomicSpy("city", "City of rock and roll"));

		addOrganisationToReturnFromSpy("organisation", organisationId, 4);
		addOrganisationAddressToReturnFromSpy("organisation_address", 4);

		address.handleDbForDataGroup(organisation);

		assertCorrectDatabaseQueriesWhenUpdatingAddress(organisationId, organisation);
	}

	private void assertCorrectDatabaseQueriesWhenUpdatingAddress(int organisationId,
			DataGroup organisation) {
		assertEquals(recordReaderFactory.factoredReaders.size(), 1);
		assertFirstReadRowIsOrganisation(organisationId);

		RecordUpdaterSpy factoredUpdater = recordUpdaterFactory.factoredUpdater;
		assertTrue(factoredUpdater.updateWasCalled);
		assertEquals(factoredUpdater.tableName, "organisation_address");

		assertEquals(factoredUpdater.conditions.get("address_id"), 4);

		Map<String, Object> values = factoredUpdater.values;
		assertValuesForCreateOrUpdateAddressAreCorrect(organisation, values);

		assertFalse(recordDeleter.deleteWasCalled);
	}

	private void assertValuesForCreateOrUpdateAddressAreCorrect(DataGroup organisation,
			Map<String, Object> values) {

		Timestamp lastUpdated = (Timestamp) values.get("last_updated");
		String lastUpdatedString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
				.format(lastUpdated);
		assertTrue(lastUpdatedString
				.matches("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\.\\d{1,3}"));

		assertEquals(values.get("city"), getAtomicValueOrEmptyString(organisation, "city"));
		assertEquals(values.get("street"), getAtomicValueOrEmptyString(organisation, "street"));
		assertEquals(values.get("postbox"), getAtomicValueOrEmptyString(organisation, "box"));
		assertEquals(values.get("postnumber"),
				getAtomicValueOrEmptyString(organisation, "postcode"));
		String countryCode = getAtomicValueOrEmptyString(organisation, "country");
		assertEquals(values.get("country_code"), countryCode.toLowerCase());
	}

	private String getAtomicValueOrEmptyString(DataGroup organisation, String nameInData) {
		return organisation.containsChildWithNameInData(nameInData)
				? organisation.getFirstAtomicValueWithNameInData(nameInData)
				: "";
	}

	@Test
	public void testStreetInDataGroupAndAddressInDatabase() {
		int organisationId = 678;
		DataGroup organisation = createDataGroupWithId("678");
		organisation.addChild(new DataAtomicSpy("street", "Hill street"));

		addOrganisationToReturnFromSpy("organisation", organisationId, 4);
		addOrganisationAddressToReturnFromSpy("organisation_address", 4);

		address.handleDbForDataGroup(organisation);

		assertCorrectDatabaseQueriesWhenUpdatingAddress(organisationId, organisation);
	}

	@Test
	public void testPostboxInDataGroupAndAddressInDatabase() {
		int organisationId = 678;
		DataGroup organisation = createDataGroupWithId("678");
		organisation.addChild(new DataAtomicSpy("box", "box21"));

		addOrganisationToReturnFromSpy("organisation", organisationId, 4);
		addOrganisationAddressToReturnFromSpy("organisation_address", 4);

		address.handleDbForDataGroup(organisation);

		assertCorrectDatabaseQueriesWhenUpdatingAddress(organisationId, organisation);
	}

	@Test
	public void testPostnumberInDataGroupAndAddressInDatabase() {
		int organisationId = 678;
		DataGroup organisation = createDataGroupWithId("678");
		organisation.addChild(new DataAtomicSpy("postcode", "90210"));

		addOrganisationToReturnFromSpy("organisation", organisationId, 4);
		addOrganisationAddressToReturnFromSpy("organisation_address", 4);

		address.handleDbForDataGroup(organisation);

		assertCorrectDatabaseQueriesWhenUpdatingAddress(organisationId, organisation);
	}

	@Test
	public void testCountryCodeInDataGroupAndAddressInDatabase() {
		int organisationId = 678;
		DataGroup organisation = createDataGroupWithId("678");
		organisation.addChild(new DataAtomicSpy("country", "SE"));

		addOrganisationToReturnFromSpy("organisation", organisationId, 4);
		addOrganisationAddressToReturnFromSpy("organisation_address", 4);

		address.handleDbForDataGroup(organisation);

		assertCorrectDatabaseQueriesWhenUpdatingAddress(organisationId, organisation);
	}

	@Test
	public void testCompleteAddressInDataGroupAndAddressInDatabase() {
		int organisationId = 678;
		DataGroup organisation = createDataGroupWithId("678");
		organisation.addChild(new DataAtomicSpy("city", "City of rock and roll"));
		organisation.addChild(new DataAtomicSpy("country", "SE"));
		organisation.addChild(new DataAtomicSpy("postcode", "90210"));
		organisation.addChild(new DataAtomicSpy("box", "box21"));
		organisation.addChild(new DataAtomicSpy("street", "Hill street"));

		addOrganisationToReturnFromSpy("organisation", organisationId, 4);
		addOrganisationAddressToReturnFromSpy("organisation_address", 4);

		address.handleDbForDataGroup(organisation);

		assertCorrectDatabaseQueriesWhenUpdatingAddress(organisationId, organisation);
	}

	@Test
	public void testAddressInDataGroupButNOAddressInDatabase() {
		int organisationId = 678;
		DataGroup organisation = createDataGroupWithId("678");
		organisation.addChild(new DataAtomicSpy("box", "box21"));

		addOrganisationToReturnFromSpy("organisation", organisationId, -1);

		address.handleDbForDataGroup(organisation);
		assertTrue(recordCreator.insertWasCalled);

		assertEquals(recordCreator.usedTableName, "organisation_address");

		RecordReaderAddressSpy sequenceReader = recordReaderFactory.factoredReaders.get(1);
		assertEquals(sequenceReader.sequenceName, "address_sequence");
		int generatedAddressKey = (int) sequenceReader.nextVal.get("nextval");
		assertEquals(recordCreator.values.get("address_id"), generatedAddressKey);

		assertValuesForCreateOrUpdateAddressAreCorrect(organisation, recordCreator.values);

		RecordUpdaterSpy factoredUpdater = recordUpdaterFactory.factoredUpdater;

		assertEquals(factoredUpdater.tableName, "organisation");
		assertEquals(factoredUpdater.values.get("address_id"), generatedAddressKey);
		assertEquals(factoredUpdater.conditions.get("organisation_id"), 678);

	}
}
