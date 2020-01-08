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

import static org.testng.Assert.assertSame;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.diva.mixedstorage.db.RecordCreatorSpy;
import se.uu.ub.cora.diva.mixedstorage.db.RecordDeleterSpy;
import se.uu.ub.cora.diva.mixedstorage.db.RecordReaderSpy;
import se.uu.ub.cora.sqldatabase.RecordCreator;
import se.uu.ub.cora.sqldatabase.RecordDeleter;
import se.uu.ub.cora.sqldatabase.RecordReader;

public class RelatedTableFactoryTest {

	private RecordReader recordReader;
	private RecordDeleter recordDeleter;
	private RecordCreator recordCreator;
	private RelatedTableFactory factory;

	@BeforeMethod
	public void setUp() {
		recordReader = new RecordReaderSpy();
		recordDeleter = new RecordDeleterSpy();
		recordCreator = new RecordCreatorSpy();
		factory = RelatedTableFactoryImp.usingReaderUpdaterAndCreator(recordReader, recordDeleter,
				recordCreator);

	}

	@Test
	public void testFactorOrganisationAlternativeName() {
		OrganisationAlternativeNameRelatedTable factoredTable = (OrganisationAlternativeNameRelatedTable) factory
				.factor("organisationAlternativeName");
		assertSame(factoredTable.getRecordReader(), recordReader);
		assertSame(factoredTable.getRecordDeleter(), recordDeleter);
		assertSame(factoredTable.getRecordCreator(), recordCreator);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "Related table not implemented for someNonExistingRelatedTable")
	public void testNotImplemented() {
		factory.factor("someNonExistingRelatedTable");
	}
}
