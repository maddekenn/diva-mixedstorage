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
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;

public class OrganisationAlternativeNameDataToDbTranslaterTest {

	@Test
	public void testOrganisationName() {
		DataGroup organisation = createDataGroupWithId("45");
		addAlternativeName(organisation, "someAlternativeName");

		OrganisationAlternativeNameDataToDbTranslater translater = new OrganisationAlternativeNameDataToDbTranslater();
		translater.translate(organisation);
		assertEquals(translater.getValues().get("organisation_name"), "someAlternativeName");
		assertEquals(translater.getValues().get("locale"), "en");
		assertEquals(translater.getValues().get("organisation_id"), 45);
		assertTrue(translater.getConditions().isEmpty());
	}

	private void addAlternativeName(DataGroup organisation, String name) {
		DataGroup alternativeName = new DataGroupSpy("alternativeName");
		alternativeName.addChild(new DataAtomicSpy("organisationName", name));
		alternativeName.addChild(new DataAtomicSpy("language", "en"));
		organisation.addChild(alternativeName);
	}

	private DataGroup createDataGroupWithId(String id) {
		DataGroup dataGroup = new DataGroupSpy("organisation");
		DataGroupSpy recordInfo = new DataGroupSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", id));
		dataGroup.addChild(recordInfo);
		return dataGroup;
	}

	@Test(expectedExceptions = DbException.class)
	public void testUpdateOrganisationIdNotAnInt() throws Exception {
		DataGroup organisation = createDataGroupWithId("notAnInt");
		DataToDbTranslater translater = new OrganisationDataToDbTranslater();
		translater.translate(organisation);

	}

	@Test
	public void testValuesAndConditionsAreOverwrittenWhenNewTranslateIsCalled() {
		OrganisationAlternativeNameDataToDbTranslater translater = new OrganisationAlternativeNameDataToDbTranslater();
		DataGroup organisation = createDataGroupWithId("45");
		addAlternativeName(organisation, "someAlternativeName");

		translater.translate(organisation);
		assertEquals(translater.getValues().get("organisation_name"), "someAlternativeName");
		assertEquals(translater.getValues().get("locale"), "en");
		assertEquals(translater.getValues().get("organisation_id"), 45);
		assertEquals(translater.getValues().size(), 3);

		DataGroup organisation2 = createDataGroupWithId("450");
		addAlternativeName(organisation2, "someAlternativeName");
		translater.translate(organisation2);
		assertEquals(translater.getValues().size(), 3);

		assertEquals(translater.getValues().get("organisation_id"), 450);
		assertEquals(translater.getValues().get("organisation_name"), "someAlternativeName");
		assertEquals(translater.getValues().get("locale"), "en");
	}

}
