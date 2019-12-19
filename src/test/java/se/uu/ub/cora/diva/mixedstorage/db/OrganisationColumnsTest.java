package se.uu.ub.cora.diva.mixedstorage.db;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class OrganisationColumnsTest {
	@Test
	public void testEnum() {
		// small hack to get 100% coverage on enum
		OrganisationColumns.valueOf(OrganisationColumns.NAME.toString());
		assertEquals(OrganisationColumns.NAME.dbName, "organisation_name");
		assertEquals(OrganisationColumns.NAME.coraName, "organisationName");
		assertEquals(OrganisationColumns.ORGANISATION_CODE.dbName, "organisation_code");
		assertEquals(OrganisationColumns.ORGANISATION_CODE.coraName, "organisationCode");
		assertEquals(OrganisationColumns.ORGANISATION_NUMBER.dbName, "orgnumber");
		assertEquals(OrganisationColumns.ORGANISATION_NUMBER.coraName, "organisationNumber");
		assertEquals(OrganisationColumns.CLOSED_DATE.dbName, "closed_date");
		assertEquals(OrganisationColumns.CLOSED_DATE.coraName, "closedDate");

	}
}
