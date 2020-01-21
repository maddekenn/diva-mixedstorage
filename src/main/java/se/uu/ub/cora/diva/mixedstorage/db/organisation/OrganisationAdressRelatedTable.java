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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTable;
import se.uu.ub.cora.sqldatabase.RecordCreator;
import se.uu.ub.cora.sqldatabase.RecordDeleter;
import se.uu.ub.cora.sqldatabase.RecordReader;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;
import se.uu.ub.cora.sqldatabase.RecordUpdater;
import se.uu.ub.cora.sqldatabase.RecordUpdaterFactory;

public class OrganisationAdressRelatedTable extends OrganisationRelatedTable
		implements RelatedTable {

	private static final String ADDRESS_ID = "address_id";
	private RecordReaderFactory recordReaderFactory;
	private RecordDeleter recordDeleter;
	private RecordUpdaterFactory recordUpdaterFactory;

	public OrganisationAdressRelatedTable(RecordReaderFactory recordReaderFactory,
			RecordDeleter recordDeleter, RecordCreator recordCreator,
			RecordUpdaterFactory recordUpdaterFactory) {
		this.recordReaderFactory = recordReaderFactory;
		this.recordDeleter = recordDeleter;
		this.recordUpdaterFactory = recordUpdaterFactory;

	}

	@Override
	public void handleDbForDataGroup(DataGroup organisation) {
		setIdAsInt(organisation);
		List<Map<String, Object>> readOrg = readOrganisationFromDb();

		Object addressIdInOrganisation = readOrg.get(0).get(ADDRESS_ID);
		if (addressExistsInDatabase(addressIdInOrganisation)) {
			int addressId = (int) addressIdInOrganisation;
			if (!organisationDataGroupContainsAddress(organisation)) {
				deleteAddressAndUpdateOrganisation(addressId);
			}
		}

		if (organisationDataGroupContainsAddress(organisation)) {
			int addressId = (int) addressIdInOrganisation;
			Map<String, Object> conditionsForAddress = createConditionWithAddressId(addressId);
			RecordReader addressReader = recordReaderFactory.factor();
			addressReader.readFromTableUsingConditions("organisation_address",
					conditionsForAddress);
		}
	}

	private List<Map<String, Object>> readOrganisationFromDb() {
		RecordReader organisationReader = recordReaderFactory.factor();
		Map<String, Object> conditions = new HashMap<>();
		conditions.put("organisation_id", organisationId);
		return organisationReader.readFromTableUsingConditions("organisation", conditions);
	}

	private void deleteAddressAndUpdateOrganisation(int addressId) {
		Map<String, Object> deleteConditions = createConditionWithAddressId(addressId);
		recordDeleter.deleteFromTableUsingConditions("organisation_address", deleteConditions);
		updateOrganisationWithNoAddressId();
	}

	private boolean addressExistsInDatabase(Object addressIdInOrganisation) {
		return addressIdInOrganisation != null;
	}

	private Map<String, Object> createConditionWithAddressId(int addressId) {
		Map<String, Object> conditionsForAddress = new HashMap<>();
		conditionsForAddress.put(ADDRESS_ID, addressId);
		return conditionsForAddress;
	}

	private void updateOrganisationWithNoAddressId() {
		Map<String, Object> values = createValuesForNullAddressId();
		Map<String, Object> updateConditions = createConditionsWithOrganisationId();
		updateAddressColumnInOrganisation(values, updateConditions);
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

	private void updateAddressColumnInOrganisation(Map<String, Object> values,
			Map<String, Object> updateConditions) {
		RecordUpdater recordUpdater = recordUpdaterFactory.factor();
		recordUpdater.updateTableUsingNameAndColumnsWithValuesAndConditions("organisation", values,
				updateConditions);
	}

	private boolean organisationDataGroupContainsAddress(DataGroup organisation) {
		return organisation.containsChildWithNameInData("city")
		// || organisation.containsChildWithNameInData("street")
		// || organisation.containsChildWithNameInData("postbox")
		// || organisation.containsChildWithNameInData("postnumber")
		// || organisation.containsChildWithNameInData("country_code")
		;
	}

	@Override
	protected Map<String, Object> createConditionsFoReadingCurrentRows() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void removeRowsNoLongerPresentInDataGroup(Set<String> idsInDatabase,
			Set<String> originalIdsFromDataGroup) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void addDataFromDataGroupNotAlreadyInDb(Set<String> idsFromDataGroup,
			Set<String> idsInDatabase) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void addToDb(Set<String> idsFromDataGroup) {
		// TODO Auto-generated method stub

	}

	@Override
	protected Set<String> getIdsForCurrentRowsInDatabase(
			List<Map<String, Object>> allCurrentRowsInDb) {
		// TODO Auto-generated method stub
		return null;
	}

}
