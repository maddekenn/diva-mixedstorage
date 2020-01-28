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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DataToDbHelper;
import se.uu.ub.cora.sqldatabase.RecordReader;

public abstract class OrganisationRelatedTable {

	protected int organisationId;
	protected RecordReader recordReader;

	protected void setIdAsInt(DataGroup organisation) {
		String organisationIdAsString = DataToDbHelper.extractIdFromDataGroup(organisation);
		DataToDbHelper.throwDbExceptionIfIdNotAnIntegerValue(organisationIdAsString);
		organisationId = Integer.valueOf(organisationIdAsString);
	}

	protected List<Map<String, Object>> getExistingParents(
			String tableName) {
		Map<String, Object> conditions = createConditionsFoReadingCurrentRows();
		return recordReader.readFromTableUsingConditions(tableName, conditions);
	}

	protected abstract Map<String, Object> createConditionsFoReadingCurrentRows();

	protected void handleDeleteAndCreate(List<Map<String, Object>> allCurrentRowsInDb,
			Set<String> idsFromDataGroup) {
		Set<String> idsInDatabase = getIdsForCurrentRowsInDatabase(allCurrentRowsInDb);

		if (idsInDatabase.isEmpty()) {
			addToDb(idsFromDataGroup);
		} else {
			Set<String> originalIdsFromDataGroup = Set.copyOf(idsFromDataGroup);
			addDataFromDataGroupNotAlreadyInDb(idsFromDataGroup, idsInDatabase);
			removeRowsNoLongerPresentInDataGroup(idsInDatabase, originalIdsFromDataGroup);
		}
	}

	protected Timestamp getCurrentTimestamp() {
		Date today = new Date();
		long time = today.getTime();
		return new Timestamp(time);

	}

	protected abstract void removeRowsNoLongerPresentInDataGroup(Set<String> idsInDatabase,
			Set<String> originalIdsFromDataGroup);

	protected abstract void addDataFromDataGroupNotAlreadyInDb(Set<String> idsFromDataGroup,
			Set<String> idsInDatabase);

	protected abstract void addToDb(Set<String> idsFromDataGroup);

	protected abstract Set<String> getIdsForCurrentRowsInDatabase(
			List<Map<String, Object>> allCurrentRowsInDb);

}
