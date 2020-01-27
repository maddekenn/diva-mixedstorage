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
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.diva.mixedstorage.db.RecordCreatorFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.RecordDeleterFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.RecordReaderFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.RecordUpdaterFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.db.ReferenceTableFactory;

public class ReferenceTableFactoryTest {

	private RecordReaderFactorySpy recordReaderFactory;
	private RecordDeleterFactorySpy recordDeleterFactory;
	private RecordCreatorFactorySpy recordCreatorFactory;
	private RecordUpdaterFactorySpy recordUpdaterFactory;
	private ReferenceTableFactory factory;

	@BeforeMethod
	public void setUp() {
		recordReaderFactory = new RecordReaderFactorySpy();
		recordDeleterFactory = new RecordDeleterFactorySpy();
		recordCreatorFactory = new RecordCreatorFactorySpy();

		factory = new ReferenceTableFactoryImp(recordCreatorFactory, recordReaderFactory,
				recordUpdaterFactory, recordDeleterFactory);

	}

	@Test
	public void testFactorOrganisationAddress() {
		OrganisationAddressTable factored = (OrganisationAddressTable) factory
				.factor("organisationAddress");
		assertTrue(factored instanceof OrganisationAddressTable);
		assertEquals(factored.getRecordReaderFactory(), recordReaderFactory);
		assertEquals(factored.getRecordCreator(), recordCreatorFactory.factored);
		assertEquals(factored.getRecordDeleter(), recordDeleterFactory.factored);
		assertEquals(factored.getRecordUpdaterFactory(), recordUpdaterFactory);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "Reference table not implemented for someNonExistingReferenceTable")
	public void testNotImplemented() {
		factory.factor("someNonExistingReferenceTable");
	}

}
