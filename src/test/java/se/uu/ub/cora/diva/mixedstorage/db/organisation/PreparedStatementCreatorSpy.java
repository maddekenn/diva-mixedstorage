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
import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.diva.mixedstorage.db.DbStatement;
import se.uu.ub.cora.diva.mixedstorage.db.PreparedStatementCreator;
import se.uu.ub.cora.diva.mixedstorage.db.PreparedStatementSpy;

public class PreparedStatementCreatorSpy implements PreparedStatementCreator {

	public List<DbStatement> dbStatements;
	public boolean createWasCalled = false;
	public List<PreparedStatement> preparedStatements;
	public boolean throwErrorFromPreparedStatement = false;
	public Connection connection;

	@Override
	public List<PreparedStatement> createFromDbStatment(List<DbStatement> dbStatements,
			Connection connection) {
		this.dbStatements = dbStatements;
		this.connection = connection;
		createWasCalled = true;
		preparedStatements = new ArrayList<>();
		for (int i = 0; i < dbStatements.size(); i++) {
			PreparedStatementSpy preparedStatementSpy = new PreparedStatementSpy();
			preparedStatementSpy.throwErrorOnExecution = throwErrorFromPreparedStatement;
			preparedStatements.add(preparedStatementSpy);
		}
		return preparedStatements;
	}

}
