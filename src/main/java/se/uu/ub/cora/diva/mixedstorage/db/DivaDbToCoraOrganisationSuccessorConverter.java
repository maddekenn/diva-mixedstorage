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

public class DivaDbToCoraOrganisationSuccessorConverter
		extends DivaDbToCoraOrganisationAncestryConverter implements DivaDbToCoraConverter {

	@Override
	public DataGroup fromMap(Map<String, Object> dbRow) {
		this.dbRow = dbRow;
		if (mandatoryValuesAreMissing()) {
			throw ConversionException.withMessageAndException(
					"Error converting organisation successor to Cora organisation successor: Map does not "
							+ "contain mandatory values for organisation id and prdecessor id",
					null);
		}
		return createDataGroup();
	}

	private DataGroup createDataGroup() {
		DataGroup closed = DataGroup.withNameInData("closed");
		addSuccessorLink(closed);
		possiblyAddClosedDate(closed);
		return closed;
	}

	private void addSuccessorLink(DataGroup closed) {
		String id = (String) dbRow.get(ORGANISATION_ID);
		DataGroup successor = createOrganisationLinkUsingLinkedRecordId(id);
		closed.addChild(successor);
	}

	private void possiblyAddClosedDate(DataGroup closed) {
		if (successorHasClosedDate()) {
			String closedDateAsString = (String) dbRow.get("closed_date");
			closed.addChild(DataAtomic.withNameInDataAndValue("closedDate", closedDateAsString));
		}
	}

	private boolean successorHasClosedDate() {
		return dbRowHasValueForKey("closed_date");
	}

}
