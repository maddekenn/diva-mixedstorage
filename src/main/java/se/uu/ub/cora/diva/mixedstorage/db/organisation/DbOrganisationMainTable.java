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
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DataToDbTranslater;
import se.uu.ub.cora.diva.mixedstorage.db.DbMainTable;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTable;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTableFactory;
import se.uu.ub.cora.sqldatabase.RecordReader;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;
import se.uu.ub.cora.sqldatabase.RecordUpdater;

public class DbOrganisationMainTable implements DbMainTable {

	private DataToDbTranslater dataToDbTranslater;
	private RecordUpdater recordUpdater;
	private RelatedTableFactory relatedTableFactory;
	private RecordReaderFactory recordReaderFactory;
	private RecordReader recordReader;

	public DbOrganisationMainTable(DataToDbTranslater dataTranslater,
			RecordReaderFactory recordReaderFactory, RecordUpdater recordUpdater,
			RelatedTableFactory relatedTableFactory) {
		this.dataToDbTranslater = dataTranslater;
		this.recordReaderFactory = recordReaderFactory;
		this.recordUpdater = recordUpdater;
		this.relatedTableFactory = relatedTableFactory;
	}

	@Override
	public void update(DataGroup dataGroup) {
		dataToDbTranslater.translate(dataGroup);
		recordReader = recordReaderFactory.factor();
		generateDbStatements(dataGroup);

	}

	private void generateDbStatements(DataGroup dataGroup) {
		Map<String, Object> readConditions = generateReadConditions();
		recordUpdater.updateTableUsingNameAndColumnsWithValuesAndConditions("organisation",
				dataToDbTranslater.getValues(), dataToDbTranslater.getConditions());

		List<Map<String, Object>> dbOrganisation = recordReader
				.readFromTableUsingConditions("divaorganisation", readConditions);

		generateDbStatementsForAlternativeName(dataGroup, dbOrganisation);
		generateDbStatementsForAddress(dataGroup, dbOrganisation);
		generateDbStatementsForParents(dataGroup, readConditions);
		generateDbStatementsForPredecessors(dataGroup, readConditions);

	}

	private Map<String, Object> generateReadConditions() {
		Map<String, Object> readConditions = new HashMap<>();
		int organisationsId = (int) dataToDbTranslater.getConditions().get("organisation_id");
		readConditions.put("organisation_id", organisationsId);
		return readConditions;
	}

	private void generateDbStatementsForAlternativeName(DataGroup dataGroup,
			List<Map<String, Object>> dbOrganisation) {
		RelatedTable alternativeName = relatedTableFactory.factor("organisationAlternativeName");
		alternativeName.handleDbForDataGroup(dataGroup, dbOrganisation);
	}

	private void generateDbStatementsForAddress(DataGroup dataGroup,
			List<Map<String, Object>> dbOrganisation) {
		RelatedTable addressTable = relatedTableFactory.factor("organisationAddress");
		addressTable.handleDbForDataGroup(dataGroup, dbOrganisation);
	}

	private void generateDbStatementsForParents(DataGroup dataGroup,
			Map<String, Object> readConditions) {
		List<Map<String, Object>> dbParents = recordReader
				.readFromTableUsingConditions("organisation_parent", readConditions);
		RelatedTable parent = relatedTableFactory.factor("organisationParent");
		parent.handleDbForDataGroup(dataGroup, dbParents);
	}

	private void generateDbStatementsForPredecessors(DataGroup dataGroup,
			Map<String, Object> readConditions) {
		List<Map<String, Object>> dbPredecessors = recordReader
				.readFromTableUsingConditions("organisationpredecessorview", readConditions);
		RelatedTable predecessor = relatedTableFactory.factor("organisationPredecessor");
		predecessor.handleDbForDataGroup(dataGroup, dbPredecessors);
	}

	public DataToDbTranslater getDataToDbTranslater() {
		// needed for test
		return dataToDbTranslater;
	}

	public RecordUpdater getRecordUpdater() {
		// needed for test
		return recordUpdater;
	}

	public RelatedTableFactory getRelatedTableFactory() {
		// needed for test
		return relatedTableFactory;
	}

}
