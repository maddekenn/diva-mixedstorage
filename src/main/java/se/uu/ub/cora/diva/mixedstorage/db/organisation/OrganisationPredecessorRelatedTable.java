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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTable;
import se.uu.ub.cora.sqldatabase.RecordCreator;
import se.uu.ub.cora.sqldatabase.RecordDeleter;
import se.uu.ub.cora.sqldatabase.RecordReader;

public class OrganisationPredecessorRelatedTable extends OrganisationRelatedTable
		implements RelatedTable {

	private static final String ORGANISATION_COMMENT = "organisationComment";
	private static final String ORGANISATION_PREDECESSOR_DESCRIPTION = "organisation_predecessor_description";
	private static final String PREDECESSOR_ID = "predecessor_id";
	private static final String ORGANISATION_PREDECESSOR_ID = "organisation_predecessor_id";
	private static final String ORGANISATION_ID = "organisation_id";
	private static final String ORGANISATION_PREDECESSOR = "organisation_predecessor";
	private RecordDeleter recordDeleter;
	private RecordCreator recordCreator;
	private Map<String, DataGroup> predecessorsInDataGroup;

	public OrganisationPredecessorRelatedTable(RecordReader recordReader,
			RecordDeleter recordDeleter, RecordCreator recordCreator) {
		this.recordReader = recordReader;
		this.recordDeleter = recordDeleter;
		this.recordCreator = recordCreator;
	}

	@Override
	public void handleDbForDataGroup(DataGroup organisation) {
		setIdAsInt(organisation);

		List<Map<String, Object>> allCurrentPredecessorsInDb = getExistingParents(
				ORGANISATION_PREDECESSOR);
		populateCollectionWithPredecessorsFromDataGroup(organisation);
		Set<String> predecessorIdsInDataGroup = createSetWithPredecessorsInDataGroupIds();

		if (predecessorIdsInDataGroup.isEmpty()) {
			deletePredecessors(allCurrentPredecessorsInDb);
		} else {
			handleDeleteAndCreate(allCurrentPredecessorsInDb, predecessorIdsInDataGroup);

		}
	}

	private void populateCollectionWithPredecessorsFromDataGroup(DataGroup organisation) {
		predecessorsInDataGroup = new HashMap<>();

		List<DataGroup> predecessors = organisation.getAllGroupsWithNameInData("formerName");
		for (DataGroup predecessor : predecessors) {
			String predecessorId = extractPredecessorId(predecessor);
			predecessorsInDataGroup.put(predecessorId, predecessor);
		}
	}

	private Set<String> createSetWithPredecessorsInDataGroupIds() {
		Set<String> predecessorIds = new HashSet<>();
		predecessorIds.addAll(predecessorsInDataGroup.keySet());
		return predecessorIds;
	}

	@Override
	protected void handleDeleteAndCreate(List<Map<String, Object>> allCurrentRowsInDb,
			Set<String> idsFromDataGroup) {
		Set<String> idsInDatabase = getIdsForCurrentRowsInDatabase(allCurrentRowsInDb);
		Set<String> existsInDbAndDataGroup = createSetWithPredecessorsInDataGroupIds();
		existsInDbAndDataGroup.retainAll(idsInDatabase);

		if (idsInDatabase.isEmpty()) {
			addToDb(idsFromDataGroup);
		} else {
			Set<String> originalIdsFromDataGroup = Set.copyOf(idsFromDataGroup);
			addDataFromDataGroupNotAlreadyInDb(idsFromDataGroup, idsInDatabase);
			removeRowsNoLongerPresentInDataGroup(idsInDatabase, originalIdsFromDataGroup);

			handlePredecessorDescriptions(existsInDbAndDataGroup);
		}
	}

	@Override
	protected Map<String, Object> createConditionsFoReadingCurrentRows() {
		return createConditionWithOrganisationId();
	}

	private String extractPredecessorId(DataGroup predecessor) {
		DataGroup organisationLink = predecessor.getFirstGroupWithNameInData("organisationLink");
		return organisationLink.getFirstAtomicValueWithNameInData("linkedRecordId");
	}

	private void deletePredecessors(List<Map<String, Object>> predecessors) {
		for (Map<String, Object> readRow : predecessors) {
			int predecessorId = (int) readRow.get(ORGANISATION_PREDECESSOR_ID);
			deletePredecessorDescription(predecessorId);
			deletePredecessor(predecessorId);
		}
	}

	private void deletePredecessorDescription(int predecessorId) {
		Map<String, Object> deleteConditions = createConditionWithOrganisationId();
		deleteConditions.put(PREDECESSOR_ID, predecessorId);
		deleteDescriptionFromDbUsingConditions(deleteConditions);
	}

	private Map<String, Object> createConditionWithOrganisationId() {
		Map<String, Object> deleteConditions = new HashMap<>();
		deleteConditions.put(ORGANISATION_ID, organisationId);
		return deleteConditions;
	}

	private void deletePredecessor(int predecessorId) {
		Map<String, Object> deleteConditions = createConditionWithOrganisationId();
		deleteConditions.put(ORGANISATION_PREDECESSOR_ID, predecessorId);
		recordDeleter.deleteFromTableUsingConditions(ORGANISATION_PREDECESSOR, deleteConditions);
	}

	@Override
	protected Set<String> getIdsForCurrentRowsInDatabase(
			List<Map<String, Object>> allCurrentPredecessorsInDb) {
		Set<String> predecessorIdsInDatabase = new HashSet<>();
		for (Map<String, Object> readRow : allCurrentPredecessorsInDb) {
			predecessorIdsInDatabase.add(String.valueOf(readRow.get(ORGANISATION_PREDECESSOR_ID)));
		}
		return predecessorIdsInDatabase;
	}

	@Override
	protected void addToDb(Set<String> predecessorIdsInDataGroup) {
		for (String predecessorId : predecessorIdsInDataGroup) {
			Map<String, Object> values = createValuesForPredecessorInsert(predecessorId);
			recordCreator.insertIntoTableUsingNameAndColumnsWithValues(ORGANISATION_PREDECESSOR,
					values);
			possiblyAddPredecessorDescription(predecessorId);

		}
	}

	private void possiblyAddPredecessorDescription(String predecessorId) {
		DataGroup dataGroup = predecessorsInDataGroup.get(predecessorId);
		if (dataGroup.containsChildWithNameInData(ORGANISATION_COMMENT)) {
			String comment = dataGroup.getFirstAtomicValueWithNameInData(ORGANISATION_COMMENT);
			createNewDescriptionInDb(predecessorId, comment);

		}
	}

	private Map<String, Object> createValuesForDescriptionCreate(String predecessorId,
			String comment) {
		Map<String, Object> descriptionValues = createConditionsForPredecessorDescription(
				predecessorId);
		Map<String, Object> nextValue = recordReader
				.readNextValueFromSequence("organisation_predecessor_description_sequence");

		descriptionValues.put(ORGANISATION_PREDECESSOR_ID, nextValue.get("nextval"));
		descriptionValues.put("last_updated", getCurrentTimestamp());
		descriptionValues.put("description", comment);
		return descriptionValues;
	}

	@Override
	protected void addDataFromDataGroupNotAlreadyInDb(Set<String> predecessorIdsInDataGroup,
			Set<String> predecessorIdsInDatabase) {
		predecessorIdsInDataGroup.removeAll(predecessorIdsInDatabase);
		addToDb(predecessorIdsInDataGroup);
	}

	@Override
	protected void removeRowsNoLongerPresentInDataGroup(Set<String> predecessorsIdsInDatabase,
			Set<String> originalPredecessorsInDataGroup) {
		predecessorsIdsInDatabase.removeAll(originalPredecessorsInDataGroup);
		for (String predecessorId : predecessorsIdsInDatabase) {
			deletePredecessor(Integer.valueOf(predecessorId));
		}
	}

	private Map<String, Object> createValuesForPredecessorInsert(String predecessorId) {
		Map<String, Object> values = createConditionWithOrganisationId();
		values.put(ORGANISATION_PREDECESSOR_ID, Integer.valueOf(predecessorId));
		return values;
	}

	private void handlePredecessorDescriptions(Set<String> existsInDbAndDataGroup) {

		for (String id : existsInDbAndDataGroup) {
			handlePredecessorDescriptionForPredecessorId(id);
		}
	}

	private void handlePredecessorDescriptionForPredecessorId(String id) {
		DataGroup dataGroup = predecessorsInDataGroup.get(id);
		Map<String, Object> conditions = createConditionsForPredecessorDescription(id);
		Map<String, Object> readDescription = readCurrentDescriptionFromDb(conditions);
		if (dataGroup.containsChildWithNameInData(ORGANISATION_COMMENT)) {
			handleDeleteAndCreatedForDescription(id, dataGroup, conditions, readDescription);
		} else {
			deleteDescriptionInDbIfExists(conditions, readDescription);
		}
	}

	private void handleDeleteAndCreatedForDescription(String id, DataGroup dataGroup,
			Map<String, Object> conditions, Map<String, Object> readDescription) {
		String descriptionInDataGroup = dataGroup
				.getFirstAtomicValueWithNameInData(ORGANISATION_COMMENT);
		if (readDescription.isEmpty()) {
			createNewDescriptionInDb(id, descriptionInDataGroup);
		} else {
			String description = (String) readDescription.get("description");
			if (!description.equals(descriptionInDataGroup)) {
				deleteDescriptionFromDbUsingConditions(conditions);
				createNewDescriptionInDb(id, descriptionInDataGroup);
			}
		}
	}

	private void deleteDescriptionInDbIfExists(Map<String, Object> conditions,
			Map<String, Object> readDescription) {
		if (!readDescription.isEmpty()) {
			deleteDescriptionFromDbUsingConditions(conditions);
		}
	}

	private void deleteDescriptionFromDbUsingConditions(Map<String, Object> conditions) {
		recordDeleter.deleteFromTableUsingConditions(ORGANISATION_PREDECESSOR_DESCRIPTION,
				conditions);
	}

	private void createNewDescriptionInDb(String id, String descriptionInDataGroup) {
		Map<String, Object> values = createValuesForDescriptionCreate(id, descriptionInDataGroup);
		recordCreator.insertIntoTableUsingNameAndColumnsWithValues(
				ORGANISATION_PREDECESSOR_DESCRIPTION, values);
	}

	private Map<String, Object> createConditionsForPredecessorDescription(String id) {
		Map<String, Object> conditions = new HashMap<>();
		conditions.put(ORGANISATION_ID, organisationId);
		conditions.put(PREDECESSOR_ID, Integer.valueOf(id));
		return conditions;
	}

	private Map<String, Object> readCurrentDescriptionFromDb(Map<String, Object> conditions) {
		List<Map<String, Object>> readRows = recordReader
				.readFromTableUsingConditions(ORGANISATION_PREDECESSOR_DESCRIPTION, conditions);
		return getRowIfOnlyOneOrEmptyMap(readRows);
	}

	private Map<String, Object> getRowIfOnlyOneOrEmptyMap(List<Map<String, Object>> readRows) {
		return readRows.size() == 1 ? readRows.get(0) : Collections.emptyMap();
	}

	public RecordReader getRecordReader() {
		return recordReader;
	}

	public RecordDeleter getRecordDeleter() {
		// needed for test
		return recordDeleter;
	}

	public RecordCreator getRecordCreator() {
		// needed for test
		return recordCreator;
	}

}
