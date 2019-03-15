/*
 * Copyright 2019 Uppsala University Library
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

import java.util.Map;

import se.uu.ub.cora.bookkeeper.data.DataAtomic;
import se.uu.ub.cora.bookkeeper.data.DataGroup;

public class DivaDbToCoraOrganisationAncestryConverter {
	protected static final String PREDECESSOR_ID = "predecessor_id";
	protected static final String ORGANISATION_ID = "organisation_id";
	protected Map<String, String> dbRow;

	protected boolean mandatoryValuesAreMissing() {
		return organisationIdIsMissing() || predecessorIdIsMissing();
	}

	private boolean organisationIdIsMissing() {
		return !dbRowHasValueForKey(ORGANISATION_ID);
	}

	protected boolean dbRowHasValueForKey(String key) {
		return dbRow.containsKey(key) && !"".equals(dbRow.get(key));
	}

	private boolean predecessorIdIsMissing() {
		return !dbRowHasValueForKey(PREDECESSOR_ID);
	}

	protected DataGroup createOrganisationLinkUsingLinkedRecordId(String organisationId) {
		DataGroup predecessor = DataGroup.withNameInData("organisationLink");
		predecessor.addChild(
				DataAtomic.withNameInDataAndValue("linkedRecordType", "divaOrganisation"));
		predecessor.addChild(DataAtomic.withNameInDataAndValue("linkedRecordId", organisationId));
		return predecessor;
	}
}