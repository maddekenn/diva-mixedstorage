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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.connection.SqlConnectionProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DataToDbTranslater;
import se.uu.ub.cora.diva.mixedstorage.db.DbMainTable;
import se.uu.ub.cora.diva.mixedstorage.db.DbStatement;
import se.uu.ub.cora.diva.mixedstorage.db.PreparedStatementCreator;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTable;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTableFactory;
import se.uu.ub.cora.sqldatabase.RecordReader;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;
import se.uu.ub.cora.sqldatabase.SqlStorageException;

public class DbOrganisationMainTable implements DbMainTable {

	private DataToDbTranslater dataToDbTranslater;
	private RelatedTableFactory relatedTableFactory;
	private RecordReaderFactory recordReaderFactory;
	private RecordReader recordReader;
	private SqlConnectionProvider connectionProvider;
	private PreparedStatementCreator preparedStatementCreator;

	public DbOrganisationMainTable(DataToDbTranslater dataTranslater,
			RecordReaderFactory recordReaderFactory, RelatedTableFactory relatedTableFactory,
			SqlConnectionProvider connectionProvider,
			PreparedStatementCreator preparedStatementCreator) {
		this.dataToDbTranslater = dataTranslater;
		this.recordReaderFactory = recordReaderFactory;
		this.relatedTableFactory = relatedTableFactory;
		this.connectionProvider = connectionProvider;
		this.preparedStatementCreator = preparedStatementCreator;

	}

	@Override
	public void update(DataGroup dataGroup) {
		dataToDbTranslater.translate(dataGroup);
		recordReader = recordReaderFactory.factor();
		updateOrganisation(dataGroup);

	}

	private void updateOrganisation(DataGroup dataGroup) {
		Map<String, Object> readConditions = generateReadConditions();
		List<Map<String, Object>> dbOrganisation = recordReader
				.readFromTableUsingConditions("divaorganisation", readConditions);

		List<DbStatement> dbStatements = generateDbStatements(dataGroup, readConditions,
				dbOrganisation);
		executeForDbStatements(dbStatements);
	}

	private Map<String, Object> generateReadConditions() {
		Map<String, Object> readConditions = new HashMap<>();
		int organisationsId = (int) dataToDbTranslater.getConditions().get("organisation_id");
		readConditions.put("organisation_id", organisationsId);
		return readConditions;
	}

	private List<DbStatement> generateDbStatements(DataGroup dataGroup,
			Map<String, Object> readConditions, List<Map<String, Object>> dbOrganisation) {
		List<DbStatement> dbStatements = new ArrayList<>();
		dbStatements.add(createDbStatementForOrganisationUpdate());
		dbStatements.addAll(generateDbStatementsForAlternativeName(dataGroup, dbOrganisation));
		dbStatements.addAll(generateDbStatementsForAddress(dataGroup, dbOrganisation));
		dbStatements.addAll(generateDbStatementsForParents(dataGroup, readConditions));
		dbStatements.addAll(generateDbStatementsForPredecessors(dataGroup, readConditions));
		return dbStatements;
	}

	private DbStatement createDbStatementForOrganisationUpdate() {
		return new DbStatement("update", "organisation", dataToDbTranslater.getValues(),
				dataToDbTranslater.getConditions());
	}

	private List<DbStatement> generateDbStatementsForAlternativeName(DataGroup dataGroup,
			List<Map<String, Object>> dbOrganisation) {
		RelatedTable alternativeName = relatedTableFactory.factor("organisationAlternativeName");
		return alternativeName.handleDbForDataGroup(dataGroup, dbOrganisation);
	}

	private List<DbStatement> generateDbStatementsForAddress(DataGroup dataGroup,
			List<Map<String, Object>> dbOrganisation) {
		RelatedTable addressTable = relatedTableFactory.factor("organisationAddress");
		return addressTable.handleDbForDataGroup(dataGroup, dbOrganisation);
	}

	private List<DbStatement> generateDbStatementsForParents(DataGroup dataGroup,
			Map<String, Object> readConditions) {
		List<Map<String, Object>> dbParents = recordReader
				.readFromTableUsingConditions("organisation_parent", readConditions);
		RelatedTable parent = relatedTableFactory.factor("organisationParent");
		return parent.handleDbForDataGroup(dataGroup, dbParents);
	}

	private List<DbStatement> generateDbStatementsForPredecessors(DataGroup dataGroup,
			Map<String, Object> readConditions) {
		List<Map<String, Object>> dbPredecessors = recordReader
				.readFromTableUsingConditions("organisationpredecessorview", readConditions);
		RelatedTable predecessor = relatedTableFactory.factor("organisationPredecessor");
		return predecessor.handleDbForDataGroup(dataGroup, dbPredecessors);
	}

	private void executeForDbStatements(List<DbStatement> dbStatements) {
		Connection connection = connectionProvider.getConnection();
		try {
			connection.setAutoCommit(false);
			createAndExecutePreparedStatements(dbStatements);
			connection.commit();
			connection.close();
		} catch (SQLException e) {
			throw SqlStorageException.withMessageAndException(
					"Error executing prepared statement: " + e.getMessage(), e);
		}
	}

	private void createAndExecutePreparedStatements(List<DbStatement> dbStatements)
			throws SQLException {
		List<PreparedStatement> preparedStatements = preparedStatementCreator
				.createFromDbStatment(dbStatements, null);
		for (PreparedStatement preparedStatement : preparedStatements) {
			preparedStatement.executeUpdate();
		}
	}

	public DataToDbTranslater getDataToDbTranslater() {
		// needed for test
		return dataToDbTranslater;
	}

	public RelatedTableFactory getRelatedTableFactory() {
		// needed for test
		return relatedTableFactory;
	}

	public RecordReaderFactory getRecordReaderFactory() {
		// needed for test
		return recordReaderFactory;
	}

	public SqlConnectionProvider getSqlConnectionProvider() {
		// needed for test
		return connectionProvider;
	}

	public PreparedStatementCreator getPreparedStatementCreator() {
		// needed for test
		return preparedStatementCreator;
	}

}
