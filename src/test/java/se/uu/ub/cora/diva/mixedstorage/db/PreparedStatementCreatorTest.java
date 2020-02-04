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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.sqldatabase.SqlStorageException;

public class PreparedStatementCreatorTest {

	private PreparedStatementCreator sqlCreator;
	private Map<String, Object> conditions;
	private Map<String, Object> values;
	private DbStatement updateDbStatement;
	private DbStatement deleteDbStatement;
	private DbStatement insertDbStatement;
	private ConnectionSpy connectionSpy;

	@BeforeMethod
	public void setUp() {
		connectionSpy = new ConnectionSpy();
		sqlCreator = new PreparedStatementCreatorImp();
		setUpDefaultValuesAndConditions();
		createStatements();
	}

	private void setUpDefaultValuesAndConditions() {
		conditions = new HashMap<>();
		values = new HashMap<>();
		values.put("name", "someName");
	}

	private void createStatements() {
		updateDbStatement = new DbStatement("update", "organisation", values, conditions);
		deleteDbStatement = new DbStatement("delete", "organisation", Collections.emptyMap(),
				conditions);
		insertDbStatement = new DbStatement("insert", "organisation", values,
				Collections.emptyMap());
	}

	@Test
	public void testUpdateNoConditions() {
		List<DbStatement> dbStatements = new ArrayList<>();
		dbStatements.add(updateDbStatement);

		List<PreparedStatement> preparedStatements = sqlCreator.createFromDbStatment(dbStatements,
				connectionSpy);

		PreparedStatementSpy preparedStatement = (PreparedStatementSpy) preparedStatements.get(0);
		assertSame(connectionSpy.preparedStatementSpy, preparedStatement);
		assertEquals(connectionSpy.sql, "UPDATE organisation SET name = ?");
		assertEquals(preparedStatement.usedSetObjects.size(), 1);
		assertEquals(preparedStatement.usedSetObjects.get("1"), "someName");
	}

	@Test
	public void testUpdateWithConditions() {
		conditions.put("id", 35);
		List<DbStatement> dbStatements = new ArrayList<>();
		dbStatements.add(updateDbStatement);
		List<PreparedStatement> preparedStatements = sqlCreator.createFromDbStatment(dbStatements,
				connectionSpy);
		PreparedStatementSpy preparedStatement = (PreparedStatementSpy) preparedStatements.get(0);
		assertSame(connectionSpy.preparedStatementSpy, preparedStatement);
		assertEquals(connectionSpy.sql, "UPDATE organisation SET name = ? WHERE id = ?");
		assertEquals(preparedStatement.usedSetObjects.size(), 2);
		assertEquals(preparedStatement.usedSetObjects.get("1"), "someName");
		assertEquals(preparedStatement.usedSetObjects.get("2"), 35);
	}

	@Test
	public void testUpdateWithMultipleValuesAndConditions() {
		values.put("address", "some address");
		conditions.put("id", 35);
		conditions.put("otherId", 3500);
		List<DbStatement> dbStatements = new ArrayList<>();
		dbStatements.add(updateDbStatement);
		List<PreparedStatement> preparedStatements = sqlCreator.createFromDbStatment(dbStatements,
				connectionSpy);
		PreparedStatementSpy preparedStatement = (PreparedStatementSpy) preparedStatements.get(0);
		assertSame(connectionSpy.preparedStatementSpy, preparedStatement);
		assertEquals(connectionSpy.sql,
				"UPDATE organisation SET address = ?, name = ? WHERE otherId = ? AND id = ?");
		assertEquals(preparedStatement.usedSetObjects.size(), 4);
		assertEquals(preparedStatement.usedSetObjects.get("1"), "some address");
		assertEquals(preparedStatement.usedSetObjects.get("2"), "someName");
		assertEquals(preparedStatement.usedSetObjects.get("3"), 3500);
		assertEquals(preparedStatement.usedSetObjects.get("4"), 35);
	}

	@Test
	public void testSetTimestampPreparedStatement() throws Exception {

		Date today = new Date();
		long time = today.getTime();
		Timestamp timestamp = new Timestamp(time);
		values.put("lastupdated", timestamp);
		List<DbStatement> dbStatements = new ArrayList<>();
		dbStatements.add(updateDbStatement);
		List<PreparedStatement> preparedStatements = sqlCreator.createFromDbStatment(dbStatements,
				connectionSpy);
		PreparedStatementSpy preparedStatement = (PreparedStatementSpy) preparedStatements.get(0);
		assertSame(connectionSpy.preparedStatementSpy, preparedStatement);
		assertEquals(preparedStatement.usedSetObjects.get("1"), "someName");
		assertTrue(preparedStatement.usedSetTimestamps.get("2") instanceof Timestamp);
	}

	@Test(expectedExceptions = SqlStorageException.class, expectedExceptionsMessageRegExp = ""
			+ "Error executing statement: UPDATE organisation SET name = \\?")
	public void testSQlException() {
		List<DbStatement> dbStatements = new ArrayList<>();
		dbStatements.add(updateDbStatement);
		connectionSpy.returnErrorConnection = true;
		sqlCreator.createFromDbStatment(dbStatements, connectionSpy);
	}

	/***************************************** DELETE ********************************************/

	@Test
	public void testDeleteWithOneCondition() {
		conditions.put("id", 35);
		List<DbStatement> dbStatements = new ArrayList<>();
		dbStatements.add(deleteDbStatement);
		List<PreparedStatement> preparedStatements = sqlCreator.createFromDbStatment(dbStatements,
				connectionSpy);
		PreparedStatementSpy preparedStatement = (PreparedStatementSpy) preparedStatements.get(0);
		assertSame(connectionSpy.preparedStatementSpy, preparedStatement);
		assertEquals(connectionSpy.sql, "DELETE FROM organisation WHERE id = ?");
		assertEquals(preparedStatement.usedSetObjects.size(), 1);
		assertEquals(preparedStatement.usedSetObjects.get("1"), 35);
	}

	@Test
	public void testDeleteWithSeveralConditions() throws Exception {
		conditions.put("id", 35);
		conditions.put("anotherId", 72);
		conditions.put("lastId", "47");
		List<DbStatement> dbStatements = new ArrayList<>();
		dbStatements.add(deleteDbStatement);
		List<PreparedStatement> preparedStatements = sqlCreator.createFromDbStatment(dbStatements,
				connectionSpy);
		PreparedStatementSpy preparedStatement = (PreparedStatementSpy) preparedStatements.get(0);
		assertSame(connectionSpy.preparedStatementSpy, preparedStatement);
		assertEquals(connectionSpy.sql,
				"DELETE FROM organisation WHERE anotherId = ? AND lastId = ? AND id = ?");
		assertEquals(preparedStatement.usedSetObjects.size(), 3);
		assertEquals(preparedStatement.usedSetObjects.get("1"), 72);
		assertEquals(preparedStatement.usedSetObjects.get("2"), "47");
		assertEquals(preparedStatement.usedSetObjects.get("3"), 35);
	}

	/***************************************** INSERT ********************************************/

	// @Test
	// public void testInsertWithOneValue() throws Exception {
	// PreparedStatementSpy preparedStatement = (PreparedStatementSpy) sqlCreator
	// .createFromDbStatment(insertDbStatement);
	// assertSame(connection.preparedStatementSpy, preparedStatement);
	// assertEquals(connection.sql, "INSERT INTO null(column) VALUES(values)");
	//
	// }
	@Test
	public void testInsertWithOneValue() throws Exception {
		List<DbStatement> dbStatements = new ArrayList<>();
		dbStatements.add(insertDbStatement);
		List<PreparedStatement> preparedStatements = sqlCreator.createFromDbStatment(dbStatements,
				connectionSpy);
		PreparedStatementSpy preparedStatement = (PreparedStatementSpy) preparedStatements.get(0);
		assertSame(connectionSpy.preparedStatementSpy, preparedStatement);
		assertEquals(connectionSpy.sql, "INSERT INTO organisation(name) VALUES(?)");
		assertCorrectPreparedStatementDefaultInsert(preparedStatement);
	}

	private void assertCorrectPreparedStatementDefaultInsert(
			PreparedStatementSpy preparedStatement) {
		assertEquals(preparedStatement.usedSetObjects.size(), 1);
		assertEquals(preparedStatement.usedSetObjects.get("1"), "someName");
	}

	@Test
	public void testInsertWithMultipleValue() throws Exception {
		values.put("address", "some address");
		values.put("alternative_name", "some other name");
		values.put("org_id", 12345);
		List<DbStatement> dbStatements = new ArrayList<>();
		dbStatements.add(insertDbStatement);
		List<PreparedStatement> preparedStatements = sqlCreator.createFromDbStatment(dbStatements,
				connectionSpy);
		PreparedStatementSpy preparedStatement = (PreparedStatementSpy) preparedStatements.get(0);
		assertSame(connectionSpy.preparedStatementSpy, preparedStatement);
		assertEquals(connectionSpy.sql,
				"INSERT INTO organisation(alternative_name, address, org_id, name) VALUES(?, ?, ?, ?)");
		assertEquals(preparedStatement.usedSetObjects.size(), 4);
		assertEquals(preparedStatement.usedSetObjects.get("1"), "some other name");
		assertEquals(preparedStatement.usedSetObjects.get("2"), "some address");
		assertEquals(preparedStatement.usedSetObjects.get("3"), 12345);
		assertEquals(preparedStatement.usedSetObjects.get("4"), "someName");

	}

	@Test
	public void testMultipleStatements() {
		conditions.put("id", 35);
		List<DbStatement> dbStatements = new ArrayList<>();
		dbStatements.add(insertDbStatement);
		dbStatements.add(updateDbStatement);
		dbStatements.add(deleteDbStatement);
		List<PreparedStatement> preparedStatements = sqlCreator.createFromDbStatment(dbStatements,
				connectionSpy);
		assertEquals(preparedStatements.size(), 3);
		assertCorrectPreparedStatementDefaultInsert(
				(PreparedStatementSpy) preparedStatements.get(0));

		assertSame(connectionSpy.createdPreparedStatements.get(0), preparedStatements.get(0));
		assertEquals(connectionSpy.sqlStatements.get(0),
				"INSERT INTO organisation(name) VALUES(?)");
		assertSame(connectionSpy.createdPreparedStatements.get(1), preparedStatements.get(1));
		assertEquals(connectionSpy.sqlStatements.get(1),
				"UPDATE organisation SET name = ? WHERE id = ?");
		assertSame(connectionSpy.createdPreparedStatements.get(2), preparedStatements.get(2));
		assertEquals(connectionSpy.sqlStatements.get(2), "DELETE FROM organisation WHERE id = ?");
	}

}
