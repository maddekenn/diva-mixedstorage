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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DbStatement;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTable;
import se.uu.ub.cora.sqldatabase.RecordCreator;
import se.uu.ub.cora.sqldatabase.RecordDeleter;
import se.uu.ub.cora.sqldatabase.RecordReader;

public class OrganisationParentRelatedTable extends OrganisationRelatedTable
		implements RelatedTable {

	private static final String ORGANISATION_PARENT = "organisation_parent";
	private static final String ORGANISATION_PARENT_ID = "organisation_parent_id";
	private static final String ORGANISATION_ID = "organisation_id";
	private RecordDeleter recordDeleter;
	private RecordCreator recordCreator;

	public OrganisationParentRelatedTable(RecordReader recordReader) {
		this.recordReader = recordReader;
	}

	@Override
	public List<DbStatement> handleDbForDataGroup(DataGroup organisation) {
		List<DbStatement> dbStatements = new ArrayList<>();
		List<Map<String, Object>> existingParents = getExistingParents(ORGANISATION_PARENT);
		Set<String> newParents = getParentIdsInDataGroup(organisation);

		if (existingParents.size() == 0 && newParents.size() == 0) {
			return dbStatements;
		}

		handleExistingParents(dbStatements, existingParents);
		handleNewParents(dbStatements, existingParents, newParents);

		return dbStatements;
	}
	// @Override
	// public List<DbStatement> handleDbForDataGroup(DataGroup organisation) {
	// setIdAsInt(organisation);
	//
	// List<DbStatement> dbStatements = new ArrayList<>();
	//
	// List<Map<String, Object>> allCurrentParentsInDb = readCurrentRowsFromDatabaseUsingTableName(
	// ORGANISATION_PARENT);
	// Set<String> parentIdsInDataGroup = getParentIdsInDataGroup(organisation);
	//
	// if (parentIdsInDataGroup.isEmpty()) {
	// deleteParents(allCurrentParentsInDb);
	// } else {
	// handleDeleteAndCreate(allCurrentParentsInDb, parentIdsInDataGroup);
	// }
	// return dbStatements;
	//
	// }

	private void handleNewParents(List<DbStatement> dbStatements,
			List<Map<String, Object>> existingParents, Set<String> newParents) {
		// TODO Auto-generated method stub
		if (newParents.isEmpty())
			deleteParents(dbStatements, existingParents);
	}

	private void deleteParents(List<DbStatement> dbStatements, List<Map<String, Object>> parents) {
		for (Map<String, Object> readRow : parents) {
			int parentId = (int) readRow.get(ORGANISATION_PARENT_ID);
			DbStatement dbStatement = new DbStatement("delete", ORGANISATION_PARENT, null, null);
			dbStatements.add(dbStatement);
		}
	}

	private void handleExistingParents(List<DbStatement> dbStatements,
			List<Map<String, Object>> existingParents) {
	}

	@Override
	protected Map<String, Object> createConditionsFoReadingCurrentRows() {
		Map<String, Object> conditions = new HashMap<>();
		conditions.put(ORGANISATION_ID, organisationId);
		return conditions;
	}

	private Set<String> getParentIdsInDataGroup(DataGroup organisation) {
		Set<String> parentIds = new HashSet<>();
		List<DataGroup> parents = organisation.getAllGroupsWithNameInData("parentOrganisation");
		for (DataGroup parent : parents) {
			String parentId = extractParentId(parent);
			parentIds.add(parentId);
		}
		return parentIds;
	}

	private String extractParentId(DataGroup parent) {
		DataGroup organisationLink = parent.getFirstGroupWithNameInData("organisationLink");
		return organisationLink.getFirstAtomicValueWithNameInData("linkedRecordId");
	}

	private void deleteParent(int organisationId, int parentId) {
		Map<String, Object> deleteConditions = new HashMap<>();
		deleteConditions.put(ORGANISATION_ID, organisationId);
		deleteConditions.put(ORGANISATION_PARENT_ID, parentId);
		recordDeleter.deleteFromTableUsingConditions(ORGANISATION_PARENT, deleteConditions);
	}

	@Override
	protected Set<String> getIdsForCurrentRowsInDatabase(
			List<Map<String, Object>> allCurrentParentsInDb) {
		Set<String> parentIdsInDatabase = new HashSet<>();
		for (Map<String, Object> readRow : allCurrentParentsInDb) {
			parentIdsInDatabase.add(String.valueOf(readRow.get(ORGANISATION_PARENT_ID)));
		}
		return parentIdsInDatabase;
	}

	@Override
	protected void addToDb(Set<String> parentIdsInDataGroup) {
		for (String parentId : parentIdsInDataGroup) {
			Map<String, Object> values = createValuesForParentInsert(parentId);
			recordCreator.insertIntoTableUsingNameAndColumnsWithValues(ORGANISATION_PARENT, values);
		}
	}

	private Map<String, Object> createValuesForParentInsert(String parentId) {
		Map<String, Object> values = new HashMap<>();
		values.put(ORGANISATION_ID, organisationId);
		values.put(ORGANISATION_PARENT_ID, Integer.valueOf(parentId));
		return values;
	}

	@Override
	protected void addDataFromDataGroupNotAlreadyInDb(Set<String> parentIdsInDataGroup,
			Set<String> parentIdsInDatabase) {
		parentIdsInDataGroup.removeAll(parentIdsInDatabase);
		addToDb(parentIdsInDataGroup);
	}

	@Override
	protected void removeRowsNoLongerPresentInDataGroup(Set<String> parentIdsInDatabase,
			Set<String> originalParentsInDataGroup) {
		parentIdsInDatabase.removeAll(originalParentsInDataGroup);
		for (String parentId : parentIdsInDatabase) {
			deleteParent(organisationId, Integer.valueOf(parentId));
		}
	}

	public RecordReader getRecordReader() {
		// needed for test
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
