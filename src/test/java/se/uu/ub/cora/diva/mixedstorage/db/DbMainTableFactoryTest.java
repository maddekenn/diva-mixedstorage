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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.DbOrganisationMainTable;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.ReferenceTableFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.RelatedTableFactorySpy;

public class DbMainTableFactoryTest {

	private DbMainTableFactory factory;
	private DataToDbTranslaterFactorySpy translaterFactory;
	private RecordUpdaterFactorySpy recordUpdaterFactory;
	private RelatedTableFactorySpy relatedTableFactory;
	private ReferenceTableFactorySpy referenceTableFactory;

	@BeforeMethod
	public void setUp() {
		translaterFactory = new DataToDbTranslaterFactorySpy();
		recordUpdaterFactory = new RecordUpdaterFactorySpy();
		relatedTableFactory = new RelatedTableFactorySpy();
		referenceTableFactory = new ReferenceTableFactorySpy();
		factory = new DbMainTableFactoryImp(translaterFactory, recordUpdaterFactory,
				relatedTableFactory, referenceTableFactory);
	}

	@Test
	public void testFactorOrganisationTable() {
		DbOrganisationMainTable mainTable = (DbOrganisationMainTable) factory
				.factor("organisation");
		assertSame(mainTable.getDataToDbTranslater(), translaterFactory.factoredTranslater);
		assertSame(mainTable.getRecordUpdater(), recordUpdaterFactory.factoredUpdater);
		assertSame(mainTable.getRelatedTableFactory(), relatedTableFactory);
		assertSame(mainTable.getReferenceTableFactory(), referenceTableFactory);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "Main table not implemented for someNonExistingMainTable")
	public void testNotImplemented() {
		factory.factor("someNonExistingMainTable");
	}
}
