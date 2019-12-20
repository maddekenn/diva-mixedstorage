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

import static org.testng.Assert.assertEquals;

import java.sql.Date;

import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;

public class OrganisationDataToDbTranslaterTest {

	@Test
	public void testConditions() {
		DataGroup dataGroup = createDataGroupWithId("56");
		DataToDbTranslater translater = new OrganisationDataToDbTranslater();

		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 56);
	}

	private DataGroup createDataGroupWithId(String id) {
		DataGroup dataGroup = new DataGroupSpy("organisation");
		DataGroupSpy recordInfo = new DataGroupSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", id));
		dataGroup.addChild(recordInfo);
		return dataGroup;
	}

	@Test
	public void testOrganisationNameInValues() {
		DataGroup dataGroup = createDataGroupWithId("45");
		dataGroup.addChild(new DataAtomicSpy("organisationName", "someChangedName"));

		DataToDbTranslater translater = new OrganisationDataToDbTranslater();
		translater.translate(dataGroup);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
	}

	@Test
	public void testUpdateAllAtomicChildrenInOrganisation() throws Exception {
		DataGroup dataGroup = createDataGroupWithId("45");
		dataGroup.addChild(new DataAtomicSpy("organisationName", "someChangedName"));
		dataGroup.addChild(new DataAtomicSpy("closedDate", "2017-10-31"));
		dataGroup.addChild(new DataAtomicSpy("organisationCode", "1235"));
		dataGroup.addChild(new DataAtomicSpy("organisationNumber", "78979-45654"));

		DataToDbTranslater translater = new OrganisationDataToDbTranslater();
		translater.translate(dataGroup);

		assertEquals(translater.getConditions().get("organisation_id"), 45);

		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");
		Date closedDate = (Date) translater.getValues().get("closed_date");
		assertEquals(closedDate, Date.valueOf("2017-10-31"));
		assertEquals(translater.getValues().get("organisation_code"), "1235");
		assertEquals(translater.getValues().get("orgnumber"), "78979-45654");
	}

	@Test
	public void testUpdateEmptyDataAtomicsAreSetToNullInQuery() throws Exception {
		DataGroup dataGroup = createDataGroupWithId("45");

		DataToDbTranslater translater = new OrganisationDataToDbTranslater();
		translater.translate(dataGroup);

		assertEquals(translater.getConditions().get("organisation_id"), 45);

		assertEquals(translater.getValues().get("organisation_name"), null);
		assertEquals(translater.getValues().get("closed_date"), null);
		assertEquals(translater.getValues().get("organisation_code"), null);
		assertEquals(translater.getValues().get("orgnumber"), null);
	}

	@Test
	public void testValuesAndConditionsAreOverwrittenWhenNewTranslateIsCalled() {
		DataGroup dataGroup = createDataGroupWithId("45");
		dataGroup.addChild(new DataAtomicSpy("organisationName", "someChangedName"));

		DataToDbTranslater translater = new OrganisationDataToDbTranslater();
		translater.translate(dataGroup);
		assertEquals(translater.getConditions().size(), 1);
		assertEquals(translater.getValues().size(), 4);
		assertEquals(translater.getConditions().get("organisation_id"), 45);
		assertEquals(translater.getValues().get("organisation_name"), "someChangedName");

		DataGroup dataGroup2 = createDataGroupWithId("4500");
		dataGroup2.addChild(new DataAtomicSpy("organisationName", "someOtherChangedName"));
		translater.translate(dataGroup2);
		assertEquals(translater.getConditions().size(), 1);
		assertEquals(translater.getValues().size(), 4);

		assertEquals(translater.getConditions().get("organisation_id"), 4500);
		assertEquals(translater.getValues().get("organisation_name"), "someOtherChangedName");
	}

	@Test(expectedExceptions = DbException.class)
	public void testUpdateOrganisationIdNotAnInt() throws Exception {
		DataGroup dataGroup = createDataGroupWithId("notAnInt");
		DataToDbTranslater translater = new OrganisationDataToDbTranslater();
		translater.translate(dataGroup);

	}
}
