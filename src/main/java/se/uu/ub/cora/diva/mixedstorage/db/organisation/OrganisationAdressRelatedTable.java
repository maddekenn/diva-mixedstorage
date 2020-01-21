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

public class OrganisationAdressRelatedTable extends OrganisationRelatedTable
		implements RelatedTable {

	private RecordReaderFactory recordReaderFactory;
	private RecordDeleter recordDeleter;
	private RecordUpdater recordUpdater;

	public OrganisationAdressRelatedTable(RecordReaderFactory recordReaderFactory,
			RecordDeleter recordDeleter, RecordCreator recordCreator, RecordUpdater recordUpdater) {
		this.recordReaderFactory = recordReaderFactory;
		this.recordDeleter = recordDeleter;
		this.recordUpdater = recordUpdater;

	}

	@Override
	public void handleDbForDataGroup(DataGroup organisation) {
		setIdAsInt(organisation);
		Map<String, Object> conditions = new HashMap<>();
		RecordReader organisationReader = recordReaderFactory.factor();
		conditions.put("organisation_id", organisationId);
		List<Map<String, Object>> readOrg = organisationReader
				.readFromTableUsingConditions("organisation", conditions);
		Object addressIdInOrganisation = readOrg.get(0).get("address_id");
		if (addressIdInOrganisation != null) {
			int addressId = (int) addressIdInOrganisation;
			if (!organisationDataGroupContainsAddress(organisation)) {
				Map<String, Object> deleteConditions = new HashMap<>();
				deleteConditions.put("address_id", addressId);
				recordDeleter.deleteFromTableUsingConditions("organisation_address",
						deleteConditions);
			}
		}

		if (organisationDataGroupContainsAddress(organisation)) {
			int addressId = (int) addressIdInOrganisation;
			Map<String, Object> conditionsForAddressRead = new HashMap<>();
			conditionsForAddressRead.put("address_id", addressId);

			RecordReader addressReader = recordReaderFactory.factor();
			addressReader.readFromTableUsingConditions("organisation_address",
					conditionsForAddressRead);
		}
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
