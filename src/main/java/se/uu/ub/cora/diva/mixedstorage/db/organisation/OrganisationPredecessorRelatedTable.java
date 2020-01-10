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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DataToDbHelper;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTable;
import se.uu.ub.cora.sqldatabase.RecordCreator;
import se.uu.ub.cora.sqldatabase.RecordDeleter;
import se.uu.ub.cora.sqldatabase.RecordReader;

public class OrganisationPredecessorRelatedTable implements RelatedTable {

	private RecordReader recordReader;
	private RecordDeleter recordDeleter;
	private RecordCreator recordCreator;
	private int organisationId;

	public OrganisationPredecessorRelatedTable(RecordReader recordReader,
			RecordDeleter recordDeleter, RecordCreator recordCreator) {
		this.recordReader = recordReader;
		this.recordDeleter = recordDeleter;
		this.recordCreator = recordCreator;
	}

	@Override
	public void handleDbForDataGroup(DataGroup organisation) {
		setIdAsInt(organisation);

		List<Map<String, Object>> allCurrentPredecessorsInDb = readCurrentPredecessorsFromDatabase();
		Set<String> predecessorIdsInDataGroup = getPredecessorIdsInDataGroup(organisation);

		if (predecessorIdsInDataGroup.isEmpty()) {
			deletePredecessors(allCurrentPredecessorsInDb);
		}
	}

	private void setIdAsInt(DataGroup organisation) {
		String organisationIdAsString = DataToDbHelper.extractIdFromDataGroup(organisation);
		DataToDbHelper.throwDbExceptionIfIdNotAnIntegerValue(organisationIdAsString);
		organisationId = Integer.valueOf(organisationIdAsString);
	}

	private List<Map<String, Object>> readCurrentPredecessorsFromDatabase() {
		Map<String, Object> conditions = createConditionsFoReadingCurrentPredecessors();
		return recordReader.readFromTableUsingConditions("organisation_predecessor", conditions);
	}

	private Map<String, Object> createConditionsFoReadingCurrentPredecessors() {
		Map<String, Object> conditions = new HashMap<>();
		conditions.put("organisation_id", organisationId);
		return conditions;
	}

	private Set<String> getPredecessorIdsInDataGroup(DataGroup organisation) {
		Set<String> predecessorIds = new HashSet<>();
		List<DataGroup> predecessors = organisation
				.getAllGroupsWithNameInData("parentOrganisation");
		for (DataGroup predecessor : predecessors) {
			String predecessorId = extractParentId(predecessor);
			predecessorIds.add(predecessorId);
		}
		return predecessorIds;
	}

	private String extractParentId(DataGroup predecessor) {
		DataGroup organisationLink = predecessor.getFirstGroupWithNameInData("organisationLink");
		return organisationLink.getFirstAtomicValueWithNameInData("linkedRecordId");
	}

	private void deletePredecessors(List<Map<String, Object>> predecessors) {
		for (Map<String, Object> readRow : predecessors) {
			int predecessorId = (int) readRow.get("organisation_predecessor_id");
			deletePredecessor(organisationId, predecessorId);
		}
	}

	private void deletePredecessor(int organisationId, int predecessorId) {
		Map<String, Object> deleteConditions = new HashMap<>();
		deleteConditions.put("organisation_id", organisationId);
		deleteConditions.put("organisation_predecessor_id", predecessorId);
		recordDeleter.deleteFromTableUsingConditions("organisation_predecessor", deleteConditions);
	}

	public RecordReader getRecordReader() {
		return recordReader;
	}

	public RecordDeleter getRecordDeleter() {
		// needed for test
		return recordDeleter;
	}

	public void setRecordDeleter(RecordDeleter recordDeleter) {
		// needed for test
		this.recordDeleter = recordDeleter;
	}

	public RecordCreator getRecordCreator() {
		// needed for test
		return recordCreator;
	}

}
