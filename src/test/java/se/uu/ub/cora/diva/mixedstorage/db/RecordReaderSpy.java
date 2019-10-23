/*
 * Copyright 2018, 2019 Uppsala University Library
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.sqldatabase.RecordReader;
import se.uu.ub.cora.sqldatabase.SqlStorageException;

public class RecordReaderSpy implements RecordReader {

	public String usedTableName;
	public List<String> usedTableNames = new ArrayList<>();
	public List<Map<String, Object>> returnedList = new ArrayList<>();
	public int noOfRecordsToReturn = 1;
	public Map<String, Object> usedConditions;
	public List<Map<String, Object>> usedConditionsList = new ArrayList<>();
	public int numOfPredecessorsToReturn = 0;
	public int numOfSuccessorsToReturn = 0;
	public int numOfParentsToReturn = 0;

	public Map<String, Object> oneRowRead;
	public List<Map<String, Object>> predecessorsToReturn = new ArrayList<>();
	public List<Map<String, Object>> successorsToReturn = new ArrayList<>();
	public List<Map<String, Object>> parentsToReturn = new ArrayList<>();

	@Override
	public List<Map<String, Object>> readAllFromTable(String tableName) {
		usedTableName = tableName;
		usedTableNames.add(usedTableName);
		// returnedList = new ArrayList<>();
		for (int i = 0; i < noOfRecordsToReturn; i++) {
			Map<String, Object> map = new HashMap<>();
			map.put("someKey" + i, "someValue" + i);
			returnedList.add(map);
		}
		return returnedList;
	}

	@Override
	public Map<String, Object> readOneRowFromDbUsingTableAndConditions(String tableName,
			Map<String, Object> conditions) {
		usedTableName = tableName;
		usedTableNames.add(usedTableName);
		usedConditions = conditions;
		usedConditionsList.add(usedConditions);
		if (conditions.containsKey("organisation_id")
				&& conditions.get("organisation_id").equals(600)) {
			throw SqlStorageException.withMessage("Error from spy");
		}
		Map<String, Object> map = new HashMap<>();
		map.put("someKey", "someValue");
		if (conditions.containsKey("id")) {
			if (conditions.get("id").equals("someIdWithClosedDate")) {
				map.put("closed_date", "2018-12-31");
			} else if (conditions.get("id").equals("someIdWithEmptyClosedDate")) {
				map.put("closed_date", "");
			}
		}
		oneRowRead = map;
		returnedList.add(map);
		return map;
	}

	@Override
	public List<Map<String, Object>> readFromTableUsingConditions(String tableName,
			Map<String, Object> conditions) {
		usedTableName = tableName;
		usedTableNames.add(usedTableName);
		usedConditions = conditions;
		usedConditionsList.add(usedConditions);
		if (numOfPredecessorsToReturn == -1 || numOfSuccessorsToReturn == -1) {
			return null;
		}
		predecessorsToReturn = createListToReturn(numOfPredecessorsToReturn);
		successorsToReturn = createListToReturn(numOfSuccessorsToReturn);
		parentsToReturn = createListToReturn(numOfParentsToReturn);

		// List<Map<String, String>> listToReturn = new ArrayList<>();
		// listToReturn.addAll(predecessorsToReturn);
		// listToReturn.addAll(successorsToReturn);

		if ("divaOrganisationParent".equals(tableName)) {
			return parentsToReturn;
		}

		if (conditions.containsKey("organisation_id")) {
			return predecessorsToReturn;
		}
		return successorsToReturn;
	}

	private List<Map<String, Object>> createListToReturn(int numToReturn) {
		List<Map<String, Object>> listToReturn = new ArrayList<>();
		for (int i = 0; i < numToReturn; i++) {
			Map<String, Object> map = new HashMap<>();
			map.put("someKey" + i, "someValue" + i);
			returnedList.add(map);
			listToReturn.add(map);
		}
		return listToReturn;
	}

}
