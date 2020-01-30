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
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.db.DataToDbTranslaterSpy;
import se.uu.ub.cora.diva.mixedstorage.db.DbMainTable;
import se.uu.ub.cora.diva.mixedstorage.db.RecordReaderFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.RecordReaderSpy;
import se.uu.ub.cora.diva.mixedstorage.db.RecordUpdaterSpy;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTableSpy;

public class DbOrganisationMainTableTest {

	private DbMainTable mainTable;
	private DataToDbTranslaterSpy dataTranslater;
	private RecordUpdaterSpy recordUpdater;
	private RelatedTableFactorySpy relatedTableFactory;
	private ReferenceTableFactorySpy referenceTableFactory;
	private RecordReaderFactorySpy recordReaderFactory;
	private DataGroup dataGroup;

	@BeforeMethod
	public void setUp() {
		dataGroup = new DataGroupSpy("organisation");
		DataGroupSpy recordInfo = new DataGroupSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", "4567"));
		dataGroup.addChild(recordInfo);
		dataTranslater = new DataToDbTranslaterSpy();
		recordReaderFactory = new RecordReaderFactorySpy();
		recordUpdater = new RecordUpdaterSpy();
		relatedTableFactory = new RelatedTableFactorySpy();
		referenceTableFactory = new ReferenceTableFactorySpy();
		mainTable = new DbOrganisationMainTable(dataTranslater, recordReaderFactory, recordUpdater,
				relatedTableFactory, referenceTableFactory);
	}

	@Test
	public void testCorrectValuesAreSentToTranslaterAndUpdater() {
		mainTable.update(dataGroup);
		assertEquals(dataTranslater.dataGroup, dataGroup);
		assertEquals(recordUpdater.tableName, "organisation");
		assertSame(dataTranslater.getValues(), recordUpdater.values);
		assertSame(dataTranslater.getConditions(), recordUpdater.conditions);
	}

	@Test
	public void testAlternativeName() {
		mainTable.update(dataGroup);

		RecordReaderSpy factoredReader = recordReaderFactory.factoredReaders.get(0);
		assertEquals(factoredReader.usedTableNames.get(0), "divaorganisation");
		assertEquals(factoredReader.usedConditionsList.get(0).get("organisation_id"), 4567);

		assertEquals(relatedTableFactory.relatedTableNames.get(0), "organisationAlternativeName");
		RelatedTableSpy firstRelatedTable = (RelatedTableSpy) relatedTableFactory.factoredRelatedTables
				.get(0);

		assertSame(firstRelatedTable.dataGroup, dataGroup);
		assertEquals(firstRelatedTable.dbRows, factoredReader.returnedListCollection.get(0));

	}

	@Test
	public void testAddress() {
		mainTable.update(dataGroup);
		// RecordReaderSpy factoredReader = recordReaderFactory.factoredReaders.get(0);
		// assertEquals(factoredReader.usedTableNames.get(3), "divaorganisation");

		assertEquals(relatedTableFactory.relatedTableNames.get(1), "organisationAddress");

		// assertEquals(referenceTableFactory.tableName, "organisationAddress");
		// ReferenceTableSpy addressTable = referenceTableFactory.factored;
		// assertSame(addressTable.organisationSentToSpy, dataGroup);
	}

	@Test
	public void testParent() {
		mainTable.update(dataGroup);

		RecordReaderSpy factoredReader = recordReaderFactory.factoredReaders.get(0);
		assertEquals(factoredReader.usedTableNames.get(1), "organisation_parent");
		assertEquals(factoredReader.usedConditionsList.get(1).get("organisation_id"), 4567);

		assertEquals(relatedTableFactory.relatedTableNames.get(2), "organisationParent");
		RelatedTableSpy secondRelatedTable = (RelatedTableSpy) relatedTableFactory.factoredRelatedTables
				.get(1);
		assertSame(secondRelatedTable.dataGroup, dataGroup);
		assertEquals(secondRelatedTable.dbRows, factoredReader.returnedListCollection.get(1));

	}

	@Test
	public void testPredecessor() {
		mainTable.update(dataGroup);
		RecordReaderSpy factoredReader = recordReaderFactory.factoredReaders.get(0);
		assertEquals(factoredReader.usedTableNames.get(2), "organisationpredecessorview");
		assertEquals(factoredReader.usedConditionsList.get(2).get("organisation_id"), 4567);

		assertEquals(relatedTableFactory.relatedTableNames.get(3), "organisationPredecessor");
		RelatedTableSpy thirdRelatedTable = (RelatedTableSpy) relatedTableFactory.factoredRelatedTables
				.get(2);
		assertSame(thirdRelatedTable.dataGroup, dataGroup);
		assertEquals(thirdRelatedTable.dbRows, factoredReader.returnedListCollection.get(2));

	}

}
