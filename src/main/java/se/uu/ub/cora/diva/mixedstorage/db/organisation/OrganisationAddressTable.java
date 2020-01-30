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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DataToDbHelper;
import se.uu.ub.cora.diva.mixedstorage.db.DbStatement;
import se.uu.ub.cora.diva.mixedstorage.db.ReferenceTable;
import se.uu.ub.cora.sqldatabase.RecordCreator;
import se.uu.ub.cora.sqldatabase.RecordDeleter;
import se.uu.ub.cora.sqldatabase.RecordReader;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;
import se.uu.ub.cora.sqldatabase.RecordUpdaterFactory;

public class OrganisationAddressTable implements ReferenceTable {

	private static final String CITY = "city";
	private static final String STREET = "street";
	private static final String ORGANISATION_ADDRESS = "organisation_address";
	private static final String ADDRESS_ID = "address_id";
	private RecordReaderFactory recordReaderFactory;
	private RecordDeleter recordDeleter;
	private RecordUpdaterFactory recordUpdaterFactory;
	private int organisationId;
	private RecordCreator recordCreator;

	public OrganisationAddressTable(RecordCreator recordCreator,
			RecordReaderFactory recordReaderFactory, RecordUpdaterFactory recordUpdaterFactory,
			RecordDeleter recordDeleter) {
		this.recordReaderFactory = recordReaderFactory;
		this.recordDeleter = recordDeleter;
		this.recordCreator = recordCreator;
		this.recordUpdaterFactory = recordUpdaterFactory;

	}

	@Override
	public List<DbStatement> handleDbForDataGroup(DataGroup organisation,
			List<Map<String, Object>> rowsFromDb) {
		setIdAsInt(organisation);
		// List<Map<String, Object>> readOrg = readOrganisationFromDb();
		List<Map<String, Object>> readOrg = rowsFromDb;
		List<DbStatement> dbStatements = new ArrayList<>();

		Object addressIdInOrganisation = readOrg.get(0).get(ADDRESS_ID);
		if (addressExistsInDatabase(addressIdInOrganisation)) {
			deleteOrUpdateAddress(dbStatements, organisation, addressIdInOrganisation);
		} else {
			possiblyInsertAddress(dbStatements, organisation);

		}
		return dbStatements;
	}

	private void deleteOrUpdateAddress(List<DbStatement> dbStatements, DataGroup organisation,
			Object addressIdInOrganisation) {
		int addressId = (int) addressIdInOrganisation;
		if (noAddressInDataGroup(organisation)) {
			deleteAddressAndUpdateOrganisation(dbStatements, addressId);
		} else {
			updateAddress(dbStatements, organisation, addressId);
		}
	}

	private void updateAddress(List<DbStatement> dbStatements, DataGroup organisation,
			int addressId) {
		Map<String, Object> values = createValuesForAddressInsertOrUpdate(organisation);
		Map<String, Object> conditions = createConditionWithAddressId(addressId);
		// RecordUpdater addressUpdater = recordUpdaterFactory.factor();
		// addressUpdater.updateTableUsingNameAndColumnsWithValuesAndConditions(ORGANISATION_ADDRESS,
		// values, conditions);
		dbStatements.add(new DbStatement("update", ORGANISATION_ADDRESS, values, conditions));
	}

	private void setIdAsInt(DataGroup organisation) {
		String organisationIdAsString = DataToDbHelper.extractIdFromDataGroup(organisation);
		DataToDbHelper.throwDbExceptionIfIdNotAnIntegerValue(organisationIdAsString);
		organisationId = Integer.valueOf(organisationIdAsString);
	}

	private List<Map<String, Object>> readOrganisationFromDb() {
		RecordReader organisationReader = recordReaderFactory.factor();
		Map<String, Object> conditions = new HashMap<>();
		conditions.put("organisation_id", organisationId);
		return organisationReader.readFromTableUsingConditions("organisation", conditions);
	}

	private boolean addressExistsInDatabase(Object addressIdInOrganisation) {
		return addressIdInOrganisation != null;
	}

	private boolean noAddressInDataGroup(DataGroup organisation) {
		return !organisationDataGroupContainsAddress(organisation);
	}

	private boolean organisationDataGroupContainsAddress(DataGroup organisation) {
		return organisation.containsChildWithNameInData(CITY)
				|| organisation.containsChildWithNameInData(STREET)
				|| organisation.containsChildWithNameInData("box")
				|| organisation.containsChildWithNameInData("postcode")
				|| organisation.containsChildWithNameInData("country");
	}

	private void deleteAddressAndUpdateOrganisation(List<DbStatement> dbStatements, int addressId) {
		updateOrganisationWithNoAddressId(dbStatements);
		Map<String, Object> deleteConditions = createConditionWithAddressId(addressId);
		// recordDeleter.deleteFromTableUsingConditions(ORGANISATION_ADDRESS, deleteConditions);
		dbStatements.add(new DbStatement("delete", ORGANISATION_ADDRESS, Collections.emptyMap(),
				deleteConditions));
	}

	private Map<String, Object> createConditionWithAddressId(int addressId) {
		Map<String, Object> conditionsForAddress = new HashMap<>();
		conditionsForAddress.put(ADDRESS_ID, addressId);
		return conditionsForAddress;
	}

	private void updateOrganisationWithNoAddressId(List<DbStatement> dbStatements) {
		Map<String, Object> values = createValuesForNullAddressId();
		Map<String, Object> updateConditions = createConditionsWithOrganisationId();
		updateAddressColumnInOrganisation(dbStatements, values, updateConditions);
	}

	private Map<String, Object> createValuesForNullAddressId() {
		Map<String, Object> values = new HashMap<>();
		values.put(ADDRESS_ID, null);
		return values;
	}

	private Map<String, Object> createConditionsWithOrganisationId() {
		Map<String, Object> updateConditions = new HashMap<>();
		updateConditions.put("organisation_id", organisationId);
		return updateConditions;
	}

	private void updateAddressColumnInOrganisation(List<DbStatement> dbStatements,
			Map<String, Object> values, Map<String, Object> updateConditions) {
		dbStatements.add(new DbStatement("update", "organisation", values, updateConditions));
	}

	private Map<String, Object> createValuesForAddressInsertOrUpdate(DataGroup organisation) {
		Map<String, Object> values = new HashMap<>();
		values.put("last_updated", getCurrentTimestamp());
		values.put(CITY, getAtomicValueOrEmptyString(organisation, CITY));
		values.put(STREET, getAtomicValueOrEmptyString(organisation, STREET));
		values.put("postbox", getAtomicValueOrEmptyString(organisation, "box"));
		values.put("postnumber", getAtomicValueOrEmptyString(organisation, "postcode"));
		values.put("country_code",
				getAtomicValueOrEmptyString(organisation, "country").toLowerCase());
		return values;
	}

	private String getAtomicValueOrEmptyString(DataGroup organisation, String nameInData) {
		return organisation.containsChildWithNameInData(nameInData)
				? organisation.getFirstAtomicValueWithNameInData(nameInData)
				: "";
	}

	private Timestamp getCurrentTimestamp() {
		Date today = new Date();
		long time = today.getTime();
		return new Timestamp(time);

	}

	private void possiblyInsertAddress(List<DbStatement> dbStatements, DataGroup organisation) {
		if (organisationDataGroupContainsAddress(organisation)) {

			RecordReader sequenceReader = recordReaderFactory.factor();
			Map<String, Object> nextValue = sequenceReader
					.readNextValueFromSequence("address_sequence");
			createInsertForAddress(dbStatements, organisation, nextValue.get("nextval"));

			Map<String, Object> values = new HashMap<>();
			values.put(ADDRESS_ID, nextValue.get("nextval"));

			updateAddressColumnInOrganisation(dbStatements, values,
					createConditionsWithOrganisationId());
		}
	}

	private void createInsertForAddress(List<DbStatement> dbStatements, DataGroup organisation,
			Object object) {
		Map<String, Object> valuesForInsert = createValuesForInsert(organisation, object);
		// recordCreator.insertIntoTableUsingNameAndColumnsWithValues(ORGANISATION_ADDRESS,
		// valuesForInsert);
		dbStatements.add(new DbStatement("insert", ORGANISATION_ADDRESS, valuesForInsert,
				Collections.emptyMap()));
	}

	private Map<String, Object> createValuesForInsert(DataGroup organisation, Object object) {
		Map<String, Object> valuesForInsert = createValuesForAddressInsertOrUpdate(organisation);
		valuesForInsert.put(ADDRESS_ID, object);
		return valuesForInsert;
	}

	@Override
	public RecordReaderFactory getRecordReaderFactory() {
		// needed for test
		return recordReaderFactory;
	}

	@Override
	public RecordCreator getRecordCreator() {
		// needed for test
		return recordCreator;
	}

	@Override
	public RecordDeleter getRecordDeleter() {
		// needed for test
		return recordDeleter;
	}

	public RecordUpdaterFactory getRecordUpdaterFactory() {
		// needed for test
		return recordUpdaterFactory;
	}

}
