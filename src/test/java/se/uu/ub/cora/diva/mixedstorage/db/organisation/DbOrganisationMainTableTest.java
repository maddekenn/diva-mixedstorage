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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.db.DataToDbTranslaterSpy;
import se.uu.ub.cora.diva.mixedstorage.db.RecordUpdaterSpy;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTableSpy;

public class DbOrganisationMainTableTest {

	private DbMainTable mainTable;
	private DataToDbTranslaterSpy dataTranslater;
	private RecordUpdaterSpy recordUpdater;
	private RelatedTableFactorySpy relatedTableFactory;

	@BeforeMethod
	public void setUp() {
		dataTranslater = new DataToDbTranslaterSpy();
		recordUpdater = new RecordUpdaterSpy();
		relatedTableFactory = new RelatedTableFactorySpy();

		mainTable = new DbOrganisationMainTable(dataTranslater, recordUpdater, relatedTableFactory);
	}

	@Test
	public void testCorrectValuesAreSentToTranslaterAndUpdater() {
		DataGroup dataGroup = new DataGroupSpy("organisation");
		mainTable.update(dataGroup);
		assertEquals(dataTranslater.dataGroup, dataGroup);
		assertEquals(recordUpdater.tableName, "organisation");
		assertSame(dataTranslater.getValues(), recordUpdater.values);
		assertSame(dataTranslater.getConditions(), recordUpdater.conditions);
	}

	@Test
	public void testAlternativeName() {
		DataGroup dataGroup = new DataGroupSpy("organisation");
		mainTable.update(dataGroup);
		assertEquals(relatedTableFactory.relatedTableNames.get(0), "organisationAlternativeName");
		RelatedTableSpy firstRelatedTable = (RelatedTableSpy) relatedTableFactory.factoredRelatedTables
				.get(0);
		assertSame(firstRelatedTable.dataGroup, dataGroup);

	}

}
