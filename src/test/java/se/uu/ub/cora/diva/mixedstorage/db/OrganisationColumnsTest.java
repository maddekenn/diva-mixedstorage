package se.uu.ub.cora.diva.mixedstorage.db;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class OrganisationColumnsTest {
	@Test
	public void testEnum() {
		// small hack to get 100% coverage on enum
		OrganisationAtomicColumns.valueOf(OrganisationAtomicColumns.NAME.toString());
		assertEquals(OrganisationAtomicColumns.NAME.dbName, "organisation_name");
		assertEquals(OrganisationAtomicColumns.NAME.coraName, "organisationName");
		assertEquals(OrganisationAtomicColumns.NAME.type, "string");
		assertEquals(OrganisationAtomicColumns.ORGANISATION_CODE.dbName, "organisation_code");
		assertEquals(OrganisationAtomicColumns.ORGANISATION_CODE.coraName, "organisationCode");
		assertEquals(OrganisationAtomicColumns.ORGANISATION_CODE.type, "string");
		assertEquals(OrganisationAtomicColumns.ORGANISATION_NUMBER.dbName, "orgnumber");
		assertEquals(OrganisationAtomicColumns.ORGANISATION_NUMBER.coraName, "organisationNumber");
		assertEquals(OrganisationAtomicColumns.ORGANISATION_NUMBER.type, "string");
		assertEquals(OrganisationAtomicColumns.CLOSED_DATE.dbName, "closed_date");
		assertEquals(OrganisationAtomicColumns.CLOSED_DATE.coraName, "closedDate");
		assertEquals(OrganisationAtomicColumns.CLOSED_DATE.type, "date");

	}
}
