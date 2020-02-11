/*
 * Copyright 2019 Uppsala University Library
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

import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.DivaDbToCoraOrganisation;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;

public class DivaDbFactoryTest {
	private DivaDbToCoraFactoryImp divaDbToCoraFactoryImp;
	private RecordReaderFactory readerFactory;
	private DivaDbToCoraConverterFactory converterFactory;

	@BeforeMethod
	public void beforeMethod() {
		readerFactory = new RecordReaderFactorySpy();
		converterFactory = new DivaDbToCoraConverterFactorySpy();

		divaDbToCoraFactoryImp = new DivaDbToCoraFactoryImp(readerFactory, converterFactory);
	}

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "No implementation found for: someType")
	public void factorUnknownTypeThrowsException() {
		divaDbToCoraFactoryImp.factor("someType");
	}

	@Test
	public void testFactoryOrganisation() {
		DivaDb divaDbToCoraOrganisation = divaDbToCoraFactoryImp.factor("divaOrganisation");
		assertTrue(divaDbToCoraOrganisation instanceof DivaDbToCoraOrganisation);
	}

	@Test
	public void testFactoryOrganisationSentInFactoriesAreSentToImplementation() {
		DivaDbToCoraOrganisation divaDbToCoraOrganisation = (DivaDbToCoraOrganisation) divaDbToCoraFactoryImp
				.factor("divaOrganisation");
		assertSame(divaDbToCoraOrganisation.getRecordReaderFactory(), readerFactory);
		assertSame(divaDbToCoraOrganisation.getConverterFactory(), converterFactory);
	}

	@Test
	public void testGetReaderFactory() {
		assertSame(divaDbToCoraFactoryImp.getReaderFactory(), readerFactory);
	}

	@Test
	public void testGetConverterFactory() {
		assertSame(divaDbToCoraFactoryImp.getConverterFactory(), converterFactory);
	}
}
