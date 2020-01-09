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
package se.uu.ub.cora.diva.mixedstorage.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.sqldatabase.DataUpdater;
import se.uu.ub.cora.sqldatabase.RecordDeleter;

public class RecordDeleterSpy implements RecordDeleter {

	public boolean deleteWasCalled = false;
	public Map<String, Object> usedConditions;
	public List<Map<String, Object>> listOfUsedConditions = new ArrayList<>();
	public String usedTableName;
	public List<String> usedTableNames = new ArrayList<>();

	@Override
	public void deleteFromTableUsingConditions(String tableName, Map<String, Object> conditions) {
		usedTableName = tableName;
		usedTableNames.add(tableName);
		usedConditions = conditions;
		listOfUsedConditions.add(usedConditions);
		deleteWasCalled = true;

	}

	@Override
	public DataUpdater getDataUpdater() {
		// TODO Auto-generated method stub
		return null;
	}

}
