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

public class OrganisationParentRelatedTable implements RelatedTable {

	private static final String ORGANISATION_ID = "organisation_id";
	private RecordReader recordReader;
	private RecordDeleter recordDeleter;
	private RecordCreator recordCreator;

	public OrganisationParentRelatedTable(RecordReader recordReader, RecordDeleter recordDeleter,
			RecordCreator recordCreator) {
		this.recordReader = recordReader;
		this.recordDeleter = recordDeleter;
		this.recordCreator = recordCreator;
	}

	@Override
	public void handleDbForDataGroup(DataGroup organisation) {
		String organisationId = DataToDbHelper.extractIdFromDataGroup(organisation);
		DataToDbHelper.throwDbExceptionIfIdNotAnIntegerValue(organisationId);
		Map<String, Object> conditions = createConditionsWithOrganisationIdAndDefaultLocale(
				organisationId);
		List<Map<String, Object>> readRows = recordReader
				.readFromTableUsingConditions("organisation_parent", conditions);
		// Set<String> setA = new HashSet<>();
		// setA.add("A");
		// setA.add("B");
		// setA.add("C");
		// setA.add("D");
		// Set<String> setB = new HashSet<>();
		// setB.add("B");
		// setB.add("C");
		// setB.add("E");
		//
		// setA.removeAll(setB);
		Set<String> parentIdsInDataGroup = getParentIdsInDataGroup(organisation);
		if (parentIdsInDataGroup.isEmpty()) {
			deleteAllParents(readRows);
		} else {
			Set<String> parentIdsInDatabase = new HashSet<>();
			for (Map<String, Object> readRow : readRows) {
				parentIdsInDatabase.add(String.valueOf(readRow.get("organisation_parent_id")));
			}
			if (parentIdsInDatabase.isEmpty()) {
				addAllParents(organisationId, parentIdsInDataGroup);
			} else {
				Set<String> originalParentsInDb = Set.copyOf(parentIdsInDatabase);
				parentIdsInDatabase.removeAll(parentIdsInDataGroup);
				addAllParents(organisationId, parentIdsInDatabase);
				// de som finns i datagroup men inte db ska göras insert på
				parentIdsInDatabase.removeAll(parentIdsInDataGroup);
				// gör insert på alla som är kvar
				// de som finns i db men inte i datagroup ska det göras delete på

			}
			// deleteParent(readRow);
		}

	}

	private void addAllParents(String organisationId, Set<String> parentIdsInDataGroup) {
		for (String parentId : parentIdsInDataGroup) {
			Map<String, Object> values = new HashMap<>();
			values.put("organisation_id", Integer.valueOf(organisationId));
			values.put("organisation_parent_id", Integer.valueOf(parentId));
			recordCreator.insertIntoTableUsingNameAndColumnsWithValues("organisation_parent",
					values);
		}
	}

	private Set<String> getParentIdsInDataGroup(DataGroup organisation) {
		List<DataGroup> parents = organisation.getAllGroupsWithNameInData("parentOrganisation");
		Set<String> parentIds = new HashSet<>();

		for (DataGroup parent : parents) {
			DataGroup organisationLink = parent.getFirstGroupWithNameInData("organisationLink");
			String parentId = organisationLink.getFirstAtomicValueWithNameInData("linkedRecordId");
			parentIds.add(parentId);
		}
		return parentIds;
	}

	private void deleteAllParents(List<Map<String, Object>> parents) {
		for (Map<String, Object> readRow : parents) {
			deleteParent(readRow);
		}
	}

	private void deleteParent(Map<String, Object> readRow) {
		Map<String, Object> deleteConditions = new HashMap<>();
		deleteConditions.put(ORGANISATION_ID, (int) readRow.get(ORGANISATION_ID));
		deleteConditions.put("organisation_parent_id", (int) readRow.get("organisation_parent_id"));
		recordDeleter.deleteFromTableUsingConditions("organisation_parent", readRow);
	}

	private Map<String, Object> createConditionsWithOrganisationIdAndDefaultLocale(
			String organisationId) {
		Map<String, Object> conditions = new HashMap<>();
		conditions.put(ORGANISATION_ID, Integer.valueOf(organisationId));
		return conditions;
	}

}
