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
import se.uu.ub.cora.diva.mixedstorage.db.DbStatement;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTable;

public class OrganisationAddressTableTest {

	private RecordReaderRelatedTableFactorySpy recordReaderFactory;
	private RelatedTable address;
	private List<Map<String, Object>> organisationRows;

	@BeforeMethod
	public void setUp() {
		recordReaderFactory = new RecordReaderRelatedTableFactorySpy();
		initOrganisationRows();
		address = new OrganisationAddressTable(recordReaderFactory);

	}

	private void initOrganisationRows() {
		organisationRows = new ArrayList<>();
		Map<String, Object> organisationRow = new HashMap<>();
		organisationRow.put("organisation_id", 678);
		organisationRow.put("address_id", 4);
		organisationRow.put("country_code", "se");
		organisationRows.add(organisationRow);
	}

	@Test
	public void testNoAddressInDataGroupNoInAddressDatabase() {
		DataGroup organisation = createDataGroupWithId("678");
		setUpOrganisationRowWithoutAddress();
		List<DbStatement> dbStatements = address.handleDbForDataGroup(organisation,
				organisationRows);
		assertTrue(dbStatements.isEmpty());
	}

	private void setUpOrganisationRowWithoutAddress() {
		organisationRows = new ArrayList<>();

		Map<String, Object> organisationRow = new HashMap<>();
		organisationRow.put("organisation_id", 678);
		organisationRows.add(organisationRow);
	}

	private DataGroup createDataGroupWithId(String id) {
		DataGroup dataGroup = new DataGroupSpy("organisation");
		DataGroupSpy recordInfo = new DataGroupSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", id));
		dataGroup.addChild(recordInfo);
		return dataGroup;
	}

	@Test
	public void testNoAddressInDataGroupButAddressInDatabase() {
		int organisationId = 678;
		DataGroup organisation = createDataGroupWithId("678");

		List<DbStatement> dbStatements = address.handleDbForDataGroup(organisation,
				organisationRows);
		assertEquals(dbStatements.size(), 2);
		DbStatement dbStatement = dbStatements.get(0);
		assertCorrectUpdateOrganisationAddressSetToNull(organisationId, dbStatement);

		assertCorrectDeletedAddress(dbStatements.get(1), 4);

	}

	private void assertCorrectUpdateOrganisationAddressSetToNull(int organisationId,
			DbStatement dbStatement) {
		assertCorrectOperationTableAndConditionForUpdateOrg(organisationId, dbStatement);
		Map<String, Object> values = dbStatement.getValues();
		assertTrue(values.containsKey("address_id"));
		assertEquals(values.get("address_id"), null);
	}

	private void assertCorrectOperationTableAndConditionForUpdateOrg(int organisationId,
			DbStatement dbStatement) {
		assertEquals(dbStatement.getOperation(), "update");
		assertEquals(dbStatement.getTableName(), "organisation");
		assertEquals(dbStatement.getConditions().get("organisation_id"), organisationId);
	}

	private void assertCorrectDeletedAddress(DbStatement dbStatement, int addressId) {
		assertEquals(dbStatement.getOperation(), "delete");
		assertEquals(dbStatement.getTableName(), "organisation_address");
		Map<String, Object> conditions = dbStatement.getConditions();
		assertEquals(conditions.get("address_id"), addressId);
		assertTrue(dbStatement.getValues().isEmpty());
	}

	@Test
	public void testCityInDataGroupAndAddressInDatabase() {
		DataGroup organisation = createDataGroupWithId("678");
		organisation.addChild(new DataAtomicSpy("city", "City of rock and roll"));

		List<DbStatement> dbStatements = address.handleDbForDataGroup(organisation,
				organisationRows);
		assertEquals(dbStatements.size(), 1);
		DbStatement dbStatement = dbStatements.get(0);
		assertCorrectDataForAddressUpdate(organisation, dbStatement, 4);

	}

	private void assertCorrectDataForAddressUpdate(DataGroup organisation, DbStatement dbStatement,
			int addressId) {
		assertEquals(dbStatement.getOperation(), "update");

		assertCorrectCommonValuesForUpdateAndInsert(organisation, dbStatement);

		Map<String, Object> conditions = dbStatement.getConditions();
		assertEquals(conditions.get("address_id"), addressId);
	}

	private void assertCorrectCommonValuesForUpdateAndInsert(DataGroup organisation,
			DbStatement dbStatement) {
		assertEquals(dbStatement.getTableName(), "organisation_address");
		Map<String, Object> values = dbStatement.getValues();
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
		DataGroup organisation = createDataGroupWithId("678");
		organisation.addChild(new DataAtomicSpy("street", "Hill street"));

		List<DbStatement> dbStatements = address.handleDbForDataGroup(organisation,
				organisationRows);
		assertEquals(dbStatements.size(), 1);
		DbStatement dbStatement = dbStatements.get(0);
		assertCorrectDataForAddressUpdate(organisation, dbStatement, 4);

	}

	@Test
	public void testPostboxInDataGroupAndAddressInDatabase() {
		DataGroup organisation = createDataGroupWithId("678");
		organisation.addChild(new DataAtomicSpy("box", "box21"));

		List<DbStatement> dbStatements = address.handleDbForDataGroup(organisation,
				organisationRows);
		assertEquals(dbStatements.size(), 1);
		DbStatement dbStatement = dbStatements.get(0);
		assertCorrectDataForAddressUpdate(organisation, dbStatement, 4);
	}

	@Test
	public void testPostnumberInDataGroupAndAddressInDatabase() {
		DataGroup organisation = createDataGroupWithId("678");
		organisation.addChild(new DataAtomicSpy("postcode", "90210"));

		List<DbStatement> dbStatements = address.handleDbForDataGroup(organisation,
				organisationRows);
		assertEquals(dbStatements.size(), 1);
		DbStatement dbStatement = dbStatements.get(0);
		assertCorrectDataForAddressUpdate(organisation, dbStatement, 4);
	}

	@Test
	public void testCountryCodeInDataGroupAndAddressInDatabase() {
		DataGroup organisation = createDataGroupWithId("678");
		organisation.addChild(new DataAtomicSpy("country", "SE"));

		List<DbStatement> dbStatements = address.handleDbForDataGroup(organisation,
				organisationRows);
		assertEquals(dbStatements.size(), 1);
		DbStatement dbStatement = dbStatements.get(0);
		assertCorrectDataForAddressUpdate(organisation, dbStatement, 4);
	}

	@Test
	public void testCompleteAddressInDataGroupAndAddressInDatabase() {
		DataGroup organisation = createDataGroupWithId("678");
		organisation.addChild(new DataAtomicSpy("city", "City of rock and roll"));
		organisation.addChild(new DataAtomicSpy("country", "SE"));
		organisation.addChild(new DataAtomicSpy("postcode", "90210"));
		organisation.addChild(new DataAtomicSpy("box", "box21"));
		organisation.addChild(new DataAtomicSpy("street", "Hill street"));

		List<DbStatement> dbStatements = address.handleDbForDataGroup(organisation,
				organisationRows);
		assertEquals(dbStatements.size(), 1);
		DbStatement dbStatement = dbStatements.get(0);
		assertCorrectDataForAddressUpdate(organisation, dbStatement, 4);
	}

	@Test
	public void testAddressInDataGroupButNOAddressInDatabase() {
		int organisationId = 678;
		DataGroup organisation = createDataGroupWithId("678");
		organisation.addChild(new DataAtomicSpy("box", "box21"));
		setUpOrganisationRowWithoutAddress();

		List<DbStatement> dbStatements = address.handleDbForDataGroup(organisation,
				organisationRows);
		assertEquals(dbStatements.size(), 2);

		RecordReaderAddressSpy sequenceReader = recordReaderFactory.factoredReaders.get(0);
		int generatedAddressKey = (int) sequenceReader.nextVal.get("nextval");

		assertEquals(sequenceReader.sequenceName, "address_sequence");
		assertCorrectDataForAddressInsert(organisation, dbStatements.get(0), 4,
				generatedAddressKey);

		DbStatement orgUpdateStatement = dbStatements.get(1);
		assertCorrectOperationTableAndConditionForUpdateOrg(organisationId, orgUpdateStatement);
		Map<String, Object> values = orgUpdateStatement.getValues();
		assertEquals(values.get("address_id"), generatedAddressKey);

	}

	private void assertCorrectDataForAddressInsert(DataGroup organisation, DbStatement dbStatement,
			int addressId, int generatedAddressKey) {
		assertEquals(dbStatement.getOperation(), "insert");
		Map<String, Object> values = dbStatement.getValues();

		assertEquals(values.get("address_id"), generatedAddressKey);
		assertCorrectCommonValuesForUpdateAndInsert(organisation, dbStatement);
	}
}
