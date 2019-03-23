package se.uu.ub.cora.diva.mixedstorage.db;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;

public class CoraToDbConverterFactoryTest {

	@Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp = ""
			+ "No converter implemented for: someType")
	public void factorUnknownTypeThrowsException() throws Exception {
		CoraToDbConverterFactory coraToDbConverterFactory = new CoraToDbConverterFactoryImp();
		coraToDbConverterFactory.factor("someType");
	}

	@Test
	public void testFactorDivaOrganisation() {
		CoraToDbConverterFactory coraToDbConverterFactory = new CoraToDbConverterFactoryImp();
		CoraToDbConverter converter = coraToDbConverterFactory.factor("divaOrganisation");
		assertTrue(converter instanceof CoraToDbOrganisationConverter);
	}

}
