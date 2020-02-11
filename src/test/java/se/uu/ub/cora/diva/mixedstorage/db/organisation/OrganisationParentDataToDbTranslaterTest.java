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
package se.uu.ub.cora.diva.mixedstorage.db.organisation;

import static org.testng.Assert.assertEquals;

import java.util.Map;

import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.db.DataToDbRepeatableTranslater;
import se.uu.ub.cora.diva.mixedstorage.db.DbException;

public class OrganisationParentDataToDbTranslaterTest {

	@Test
	public void testOrganisationParent() {
		DataGroup organisation = createDataGroupWithId("45");
		addParent(organisation, "1100");

		OrganisationParentDataToDbTranslater translater = new OrganisationParentDataToDbTranslater();
		translater.translate(organisation);
		assertEquals(translater.getRepeatableValues().size(), 1);

		Map<String, Object> firstParent = translater.getRepeatableValues().get(0);
		assertEquals(firstParent.get("organisation_parent_id"), 1100);
		assertEquals(firstParent.get("organisation_id"), 45);
	}

	private DataGroup createDataGroupWithId(String id) {
		DataGroup dataGroup = new DataGroupSpy("organisation");
		DataGroupSpy recordInfo = new DataGroupSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", id));
		dataGroup.addChild(recordInfo);
		return dataGroup;
	}

	private void addParent(DataGroup organisation, String parentId) {
		DataGroup parent = new DataGroupSpy("parentOrganisation");
		DataGroupSpy parentLink = new DataGroupSpy("organisationLink");
		parentLink.addChild(new DataAtomicSpy("linkedRecordType", "divaOrganisation"));
		parentLink.addChild(new DataAtomicSpy("linkedRecordId", parentId));
		parent.addChild(parentLink);
		organisation.addChild(parent);
	}

	@Test
	public void testOrganisationParentMultipleParents() {
		DataGroup organisation = createDataGroupWithId("45");
		addParent(organisation, "1100");
		addParent(organisation, "2200");

		OrganisationParentDataToDbTranslater translater = new OrganisationParentDataToDbTranslater();
		translater.translate(organisation);
		assertEquals(translater.getRepeatableValues().size(), 2);

		Map<String, Object> firstParent = translater.getRepeatableValues().get(0);
		assertEquals(firstParent.get("organisation_parent_id"), 1100);
		assertEquals(firstParent.get("organisation_id"), 45);

		Map<String, Object> secondParent = translater.getRepeatableValues().get(1);
		assertEquals(secondParent.get("organisation_parent_id"), 2200);
		assertEquals(secondParent.get("organisation_id"), 45);
	}

	@Test(expectedExceptions = DbException.class)
	public void testTranslateParentOrganisationIdNotAnInt() throws Exception {
		DataGroup organisation = createDataGroupWithId("notAnInt");
		addParent(organisation, "2200");
		DataToDbRepeatableTranslater translater = new OrganisationParentDataToDbTranslater();
		translater.translate(organisation);

	}

	@Test(expectedExceptions = DbException.class)
	public void testTranslateParentParentIdNotAnInt() throws Exception {
		DataGroup organisation = createDataGroupWithId("45");
		addParent(organisation, "notAnInt");
		DataToDbRepeatableTranslater translater = new OrganisationParentDataToDbTranslater();
		translater.translate(organisation);

	}

	@Test
	public void testListIsOverwrittenWhenNewTranslateIsCalled() {
		OrganisationParentDataToDbTranslater translater = new OrganisationParentDataToDbTranslater();
		DataGroup organisation = createDataGroupWithId("45");
		addParent(organisation, "2200");

		translater.translate(organisation);
		assertEquals(translater.getRepeatableValues().size(), 1);
		Map<String, Object> firstParent = translater.getRepeatableValues().get(0);
		assertEquals(firstParent.get("organisation_parent_id"), 2200);
		assertEquals(firstParent.get("organisation_id"), 45);

		DataGroup organisation2 = createDataGroupWithId("450");
		addParent(organisation2, "1100");
		translater.translate(organisation2);

		Map<String, Object> secondParent = translater.getRepeatableValues().get(0);
		assertEquals(translater.getRepeatableValues().size(), 1);
		assertEquals(secondParent.get("organisation_parent_id"), 1100);
		assertEquals(secondParent.get("organisation_id"), 450);
	}

}
