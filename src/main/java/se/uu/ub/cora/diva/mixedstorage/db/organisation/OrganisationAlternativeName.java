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
package se.uu.ub.cora.diva.mixedstorage.db.organisation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DataToDbHelper;
import se.uu.ub.cora.sqldatabase.RecordDeleter;
import se.uu.ub.cora.sqldatabase.RecordReader;

public class OrganisationAlternativeName {

	private RecordReader recordReader;
	private RecordDeleter recordDeleter;

	public OrganisationAlternativeName(RecordReader recordReader, RecordDeleter recordDeleter) {
		this.recordReader = recordReader;
		this.recordDeleter = recordDeleter;

	}

	public void handleDbForDataGroup(DataGroup organisation) {
		String id = DataToDbHelper.extractIdFromDataGroup(organisation);
		DataToDbHelper.throwDbExceptionIfIdNotAnIntegerValue(id);
		Map<String, Object> conditions = new HashMap<>();
		conditions.put("locale", "en");
		conditions.put("organisation_id", Integer.valueOf(id));
		List<Map<String, Object>> readRows = recordReader
				.readFromTableUsingConditions("organisation_name", conditions);
		for (Map<String, Object> readRow : readRows) {
			Map<String, Object> deleteConditions = new HashMap<>();
			int nameId = (int) readRow.get("organisation_name_id");
			deleteConditions.put("organisation_name_id", nameId);
			recordDeleter.deleteFromTableUsingConditions("", deleteConditions);
		}

	}

}
