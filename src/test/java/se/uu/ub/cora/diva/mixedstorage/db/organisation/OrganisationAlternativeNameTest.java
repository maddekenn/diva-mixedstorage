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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;

public class OrganisationAlternativeNameTest {

	private RecordReaderAlternativeNameSpy recordReader;
	private RecordDeleterSpy recordDeleter;
	private RecordCreatorSpy recordCreator;
	private OrganisationAlternativeName alternativeName;

	@BeforeMethod
	public void setUp() {
		recordReader = new RecordReaderAlternativeNameSpy();
		recordDeleter = new RecordDeleterSpy();
		recordCreator = new RecordCreatorSpy();
		alternativeName = new OrganisationAlternativeName(recordReader, recordDeleter,
				recordCreator);

	}

	@Test
	public void testInit() {
		DataGroup organisation = createDataGroupWithId("678");
		alternativeName.handleDbForDataGroup(organisation);
		assertEquals(recordReader.usedTableName, "organisation_name");
		assertEquals(recordReader.usedConditions.get("locale"), "en");
		assertEquals(recordReader.usedConditions.get("organisation_id"), 678);
	}

	private DataGroup createDataGroupWithId(String id) {
		DataGroup dataGroup = new DataGroupSpy("organisation");
		DataGroupSpy recordInfo = new DataGroupSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", id));
		dataGroup.addChild(recordInfo);
		return dataGroup;
	}

	@Test
	public void testNoNameInDbNoNameInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		alternativeName.handleDbForDataGroup(organisation);
		assertEquals(recordReader.usedTableName, "organisation_name");
		assertEquals(recordReader.usedConditions.get("locale"), "en");
		assertEquals(recordReader.usedConditions.get("organisation_id"), 678);
		assertFalse(recordDeleter.deleteWasCalled);
		assertFalse(recordCreator.insertWasCalled);
	}

	@Test
	public void testNoNameInDbIncompleteNameInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		DataGroupSpy alternativeNameGroup = new DataGroupSpy("alternativeName");
		organisation.addChild(alternativeNameGroup);

		alternativeName.handleDbForDataGroup(organisation);
		assertEquals(recordReader.usedTableName, "organisation_name");
		assertEquals(recordReader.usedConditions.get("locale"), "en");
		assertEquals(recordReader.usedConditions.get("organisation_id"), 678);
		assertFalse(recordDeleter.deleteWasCalled);
		assertFalse(recordCreator.insertWasCalled);
	}

	@Test
	public void testOneNameInDbButNoNameInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");

		addNameToReturnFromSpy(234, 678);

		alternativeName.handleDbForDataGroup(organisation);
		assertEquals(recordReader.usedTableName, "organisation_name");
		assertEquals(recordReader.usedConditions.get("locale"), "en");
		assertEquals(recordReader.usedConditions.get("organisation_id"), 678);
		assertTrue(recordDeleter.deleteWasCalled);

		assertEquals(recordDeleter.usedTableName, "organisation_name");
		assertEquals(recordDeleter.usedConditions.get("organisation_name_id"), 234);

		assertFalse(recordCreator.insertWasCalled);

	}

	private void addNameToReturnFromSpy(int nameId, int organisationId) {
		Map<String, Object> nameToReturn = recordReader.nameToReturn;
		nameToReturn.put("organisation_name_id", nameId);
		nameToReturn.put("organisation_id", organisationId);
		nameToReturn.put("organisation_name", "some english name");
		nameToReturn.put("locale", "en");
	}

	@Test
	public void testOneNameInDbSameNameInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		DataGroupSpy alternativeNameGroup = new DataGroupSpy("alternativeName");
		alternativeNameGroup.addChild(new DataAtomicSpy("organisationName", "some english name"));
		alternativeNameGroup.addChild(new DataAtomicSpy("language", "en"));
		organisation.addChild(alternativeNameGroup);

		addNameToReturnFromSpy(234, 678);

		alternativeName.handleDbForDataGroup(organisation);
		assertEquals(recordReader.usedTableName, "organisation_name");
		assertEquals(recordReader.usedConditions.get("locale"), "en");
		assertEquals(recordReader.usedConditions.get("organisation_id"), 678);
		assertFalse(recordDeleter.deleteWasCalled);

		assertFalse(recordCreator.insertWasCalled);

	}

	@Test
	public void testOneNameInDbDifferentNameInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		DataGroupSpy alternativeNameGroup = new DataGroupSpy("alternativeName");
		alternativeNameGroup
				.addChild(new DataAtomicSpy("organisationName", "some other english name"));
		alternativeNameGroup.addChild(new DataAtomicSpy("language", "en"));
		organisation.addChild(alternativeNameGroup);

		addNameToReturnFromSpy(234, 678);

		alternativeName.handleDbForDataGroup(organisation);
		assertEquals(recordReader.usedTableName, "organisation_name");
		assertEquals(recordReader.usedConditions.get("locale"), "en");
		assertEquals(recordReader.usedConditions.get("organisation_id"), 678);
		assertTrue(recordDeleter.deleteWasCalled);

		assertTrue(recordCreator.insertWasCalled);
		assertEquals(recordCreator.usedTableName, "organisation_name");
		assertEquals((String) recordCreator.values.get("organisation_name_id"),
				"NEXTVAL('name_sequence')");
		assertEquals(recordCreator.values.get("locale"), "en");
		assertEquals(recordCreator.values.get("organisation_name"), "some other english name");
		assertEquals(recordCreator.values.get("organisation_id"), 678);

	}

	@Test
	public void testNoNameInDbButNameInDataGroup() {
		DataGroup organisation = createDataGroupWithId("678");
		DataGroupSpy alternativeNameGroup = new DataGroupSpy("alternativeName");
		alternativeNameGroup.addChild(new DataAtomicSpy("organisationName", "some english name"));
		alternativeNameGroup.addChild(new DataAtomicSpy("language", "en"));
		organisation.addChild(alternativeNameGroup);

		alternativeName.handleDbForDataGroup(organisation);
		assertEquals(recordReader.usedTableName, "organisation_name");
		assertEquals(recordReader.usedConditions.get("locale"), "en");
		assertEquals(recordReader.usedConditions.get("organisation_id"), 678);
		assertFalse(recordDeleter.deleteWasCalled);

		assertTrue(recordCreator.insertWasCalled);
		assertEquals(recordCreator.usedTableName, "organisation_name");
		assertEquals((String) recordCreator.values.get("organisation_name_id"),
				"NEXTVAL('name_sequence')");
		assertEquals(recordCreator.values.get("locale"), "en");
		assertEquals(recordCreator.values.get("organisation_name"), "some english name");
		assertEquals(recordCreator.values.get("organisation_id"), 678);
		// TODO:kolla last updated

	}
}
