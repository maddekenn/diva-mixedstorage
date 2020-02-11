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

import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.connection.SqlConnectionProvider;
import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.OrganisationDbRecordStorage;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.RelatedTableFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.SqlConnectionProviderSpy;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;

public class RecordStorageForOneTypeFactoryTest {

	private RecordStorageForOneTypeFactory factory;
	private DataToDbTranslaterFactorySpy translaterFactory;
	private RelatedTableFactorySpy relatedTableFactory;
	private RecordReaderFactory recordReaderFactory;
	private SqlConnectionProvider sqlConnectionProvider;

	@BeforeMethod
	public void setUp() {
		translaterFactory = new DataToDbTranslaterFactorySpy();
		relatedTableFactory = new RelatedTableFactorySpy();
		recordReaderFactory = new RecordReaderFactorySpy();
		sqlConnectionProvider = new SqlConnectionProviderSpy();
		factory = new RecordStorageForOneTypeFactoryImp(translaterFactory, recordReaderFactory,
				relatedTableFactory, sqlConnectionProvider);
	}

	@Test
	public void testFactorOrganisationTable() {
		var organisationDbRecordStorage = (OrganisationDbRecordStorage) factory
				.factor("organisation");
		assertSame(organisationDbRecordStorage.getDataToDbTranslater(),
				translaterFactory.factoredTranslater);
		assertSame(organisationDbRecordStorage.getRelatedTableFactory(), relatedTableFactory);
		assertSame(organisationDbRecordStorage.getRecordReaderFactory(), recordReaderFactory);
		assertSame(organisationDbRecordStorage.getSqlConnectionProvider(), sqlConnectionProvider);
		assertTrue(organisationDbRecordStorage
				.getPreparedStatementCreator() instanceof PreparedStatementExecutorImp);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "Main table not implemented for someNonExistingRecordStorageForOneType")
	public void testNotImplemented() {
		factory.factor("someNonExistingRecordStorageForOneType");
	}
}
