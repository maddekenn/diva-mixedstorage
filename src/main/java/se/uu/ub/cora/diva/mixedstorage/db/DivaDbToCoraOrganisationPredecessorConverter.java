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

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataGroup;

public class DivaDbToCoraOrganisationPredecessorConverter
		extends DivaDbToCoraOrganisationAncestryConverter implements DivaDbToCoraConverter {

	private static final String DESCRIPTION = "description";

	@Override
	public DataGroup fromMap(Map<String, Object> dbRow) {
		this.dbRow = dbRow;
		if (mandatoryValuesAreMissing()) {
			throw ConversionException.withMessageAndException(
					"Error converting organisation predecessor to Cora organisation predecessor: Map does not "
							+ "contain mandatory values for organisation id and predecessor id",
					null);
		}
		return createDataGroup();
	}

	private DataGroup createDataGroup() {
		DataGroup formerName = DataGroup.withNameInData("formerName");
		addPredecessorLink(formerName);
		possiblyAddDescription(formerName);
		return formerName;
	}

	private void addPredecessorLink(DataGroup formerName) {
		DataGroup predecessor = createOrganisationLinkUsingLinkedRecordId(
				(String) dbRow.get(PREDECESSOR_ID));
		formerName.addChild(predecessor);
	}

	private void possiblyAddDescription(DataGroup formerName) {
		if (predecessorHasDescription()) {
			formerName.addChild(DataAtomic.withNameInDataAndValue("organisationComment",
					(String) dbRow.get(DESCRIPTION)));
		}
	}

	private boolean predecessorHasDescription() {
		return dbRowHasValueForKey(DESCRIPTION);
	}
}
