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

import java.util.Map;

public class DbStatement {

	private final String operation;
	private final String tableName;
	private final Map<String, Object> values;
	private final Map<String, Object> conditions;

	public DbStatement(String operation, String tableName, Map<String, Object> values,
			Map<String, Object> conditions) {
		this.operation = operation;
		this.tableName = tableName;
		this.values = values;
		this.conditions = conditions;
	}

	public String getOperation() {
		return operation;
	}

	public String getTableName() {
		return tableName;
	}

	public Map<String, Object> getValues() {
		return values;
	}

	public Map<String, Object> getConditions() {
		return conditions;
	}

}
