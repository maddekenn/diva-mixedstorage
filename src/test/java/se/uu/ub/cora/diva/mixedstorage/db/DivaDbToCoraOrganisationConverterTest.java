/*
 * Copyright 2018, 2019 Uppsala University Library
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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;
import se.uu.ub.cora.diva.mixedstorage.fedora.DataGroupFactorySpy;

public class DivaDbToCoraOrganisationConverterTest {

	private DivaDbToCoraOrganisationConverter converter;
	private Map<String, Object> rowFromDb;
	private DataGroupFactorySpy dataGroupFactorySpy;
	private DataAtomicFactorySpy dataAtomicFactorySpy;

	@BeforeMethod
	public void beforeMethod() {
		dataGroupFactorySpy = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactorySpy);
		dataAtomicFactorySpy = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactorySpy);
		rowFromDb = new HashMap<>();
		rowFromDb.put("id", "someOrgId");
		converter = new DivaDbToCoraOrganisationConverter();
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation to Cora organisation: Map does not contain value for id")
	public void testEmptyMap() {
		rowFromDb = new HashMap<>();
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertNull(organisation);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation to Cora organisation: Map does not contain value for id")
	public void testMapWithEmptyValueThrowsError() {
		rowFromDb = new HashMap<>();
		rowFromDb.put("id", "");
		converter.fromMap(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation to Cora organisation: Map does not contain value for id")
	public void testMapWithNonEmptyValueANDEmptyValueThrowsError() {
		Map<String, Object> rowFromDb = new HashMap<>();
		rowFromDb.put("defaultname", "someName");
		rowFromDb.put("id", "");
		converter.fromMap(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation to Cora organisation: Map does not contain value for id")
	public void mapDoesNotContainOrganisationIdValue() {
		rowFromDb = new HashMap<>();
		rowFromDb.put("defaultname", "someName");
		converter.fromMap(rowFromDb);
	}

	@Test
	public void testMinimalValuesReturnsDataGroupWithCorrectRecordInfo() {
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getNameInData(), "organisation");
		assertCorrectRecordInfoWithId(organisation, "someOrgId");

		DataGroupSpy factoredOrganisation = dataGroupFactorySpy.factoredDataGroups.get(0);
		assertEquals(factoredOrganisation.nameInData, "organisation");

		DataGroupSpy factoredRecordInfo = dataGroupFactorySpy.factoredDataGroups.get(1);
		assertEquals(factoredRecordInfo.nameInData, "recordInfo");
		assertSame(factoredRecordInfo, organisation.getFirstChildWithNameInData("recordInfo"));

		DataAtomicSpy factoredDataAtomicForId = dataAtomicFactorySpy.factoredDataAtomics.get(0);
		assertEquals(factoredDataAtomicForId.nameInData, "id");
		assertEquals(factoredDataAtomicForId.value, "someOrgId");
	}

	@Test
	public void testOrganisationName() {
		rowFromDb.put("defaultname", "Java-fakulteten");
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getNameInData(), "organisation");
		assertEquals(organisation.getFirstAtomicValueWithNameInData("organisationName"),
				"Java-fakulteten");
	}

	@Test
	public void testTypeCode() {
		rowFromDb.put("type_code", "unit");
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getFirstAtomicValueWithNameInData("organisationType"), "unit");
	}

	@Test
	public void testAlternativeName() {
		rowFromDb.put("alternative_name", "Java Faculty");
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertTrue(organisation.containsChildWithNameInData("alternativeName"));
		DataGroup alternativeName = organisation.getFirstGroupWithNameInData("alternativeName");
		assertEquals(alternativeName.getFirstAtomicValueWithNameInData("language"), "en");
		assertEquals(alternativeName.getFirstAtomicValueWithNameInData("organisationName"),
				"Java Faculty");
	}

	@Test
	public void testOrganisationNotEligible() {
		rowFromDb.put("not_eligible", true);
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getFirstAtomicValueWithNameInData("eligible"), "no");
	}

	@Test
	public void testOrganisationEligible() {
		rowFromDb.put("not_eligible", false);
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getFirstAtomicValueWithNameInData("eligible"), "yes");
	}

	private void assertCorrectRecordInfoWithId(DataGroup organisation, String id) {
		DataGroup recordInfo = organisation.getFirstGroupWithNameInData("recordInfo");
		assertEquals(recordInfo.getFirstAtomicValueWithNameInData("id"), id);

		DataGroup type = recordInfo.getFirstGroupWithNameInData("type");
		assertEquals(type.getFirstAtomicValueWithNameInData("linkedRecordType"), "recordType");
		assertEquals(type.getFirstAtomicValueWithNameInData("linkedRecordId"), "divaOrganisation");

		DataGroup dataDivider = recordInfo.getFirstGroupWithNameInData("dataDivider");
		assertEquals(dataDivider.getFirstAtomicValueWithNameInData("linkedRecordType"), "system");
		assertEquals(dataDivider.getFirstAtomicValueWithNameInData("linkedRecordId"), "diva");

		assertCorrectCreatedAndUpdatedInfo(recordInfo);
	}

	private void assertCorrectCreatedAndUpdatedInfo(DataGroup recordInfo) {
		assertEquals(recordInfo.getFirstAtomicValueWithNameInData("tsCreated"),
				"2017-01-01 00:00:00.0");

		DataGroup createdBy = recordInfo.getFirstGroupWithNameInData("createdBy");
		assertEquals(createdBy.getFirstAtomicValueWithNameInData("linkedRecordType"), "coraUser");
		assertEquals(createdBy.getFirstAtomicValueWithNameInData("linkedRecordId"),
				"coraUser:4412982402853626");

		assertEquals(recordInfo.getAllGroupsWithNameInData("updated").size(), 1);
		DataGroup updated = recordInfo.getFirstGroupWithNameInData("updated");
		assertEquals(updated.getFirstAtomicValueWithNameInData("tsUpdated"),
				"2017-01-01 00:00:00.0");
		assertEquals(updated.getRepeatId(), "0");

		DataGroup updatedBy = updated.getFirstGroupWithNameInData("updatedBy");
		assertEquals(updatedBy.getFirstAtomicValueWithNameInData("linkedRecordType"), "coraUser");
		assertEquals(updatedBy.getFirstAtomicValueWithNameInData("linkedRecordId"),
				"coraUser:4412982402853626");

	}

	@Test
	public void testAdressMissing() {
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("city"));
		assertFalse(organisation.containsChildWithNameInData("street"));
		assertFalse(organisation.containsChildWithNameInData("box"));
		assertFalse(organisation.containsChildWithNameInData("postcode"));

		// default value, since country is mandatory
		assertEquals(organisation.getFirstAtomicValueWithNameInData("country"), "SE");
	}

	@Test
	public void testAdress() {
		rowFromDb.put("city", "uppsala");
		rowFromDb.put("street", "Övre slottsgatan 1");
		rowFromDb.put("box", "Box5435");
		rowFromDb.put("postnumber", "345 34");
		rowFromDb.put("country_code", "fi");

		DataGroup organisation = converter.fromMap(rowFromDb);
		DataAtomicSpy factoredDataAtomicForCity = dataAtomicFactorySpy.factoredDataAtomics.get(15);
		assertEquals(factoredDataAtomicForCity.nameInData, "city");
		assertEquals(factoredDataAtomicForCity.value, "uppsala");

		DataAtomicSpy factoredDataAtomicForStreet = dataAtomicFactorySpy.factoredDataAtomics
				.get(16);
		assertEquals(factoredDataAtomicForStreet.nameInData, "street");
		assertEquals(factoredDataAtomicForStreet.value, "Övre slottsgatan 1");

		DataAtomicSpy factoredDataAtomicForBox = dataAtomicFactorySpy.factoredDataAtomics.get(17);
		assertEquals(factoredDataAtomicForBox.nameInData, "box");
		assertEquals(factoredDataAtomicForBox.value, "Box5435");

		DataAtomicSpy factoredDataAtomicForPostcode = dataAtomicFactorySpy.factoredDataAtomics
				.get(18);
		assertEquals(factoredDataAtomicForPostcode.nameInData, "postcode");
		assertEquals(factoredDataAtomicForPostcode.value, "345 34");

		DataAtomicSpy factoredDataAtomicForCountry = dataAtomicFactorySpy.factoredDataAtomics
				.get(19);
		assertEquals(factoredDataAtomicForCountry.nameInData, "country");
		assertEquals(factoredDataAtomicForCountry.value, "FI");

		assertEquals(organisation.getFirstAtomicValueWithNameInData("city"), "uppsala");
		assertEquals(organisation.getFirstAtomicValueWithNameInData("street"),
				"Övre slottsgatan 1");
		assertEquals(organisation.getFirstAtomicValueWithNameInData("box"), "Box5435");
		assertEquals(organisation.getFirstAtomicValueWithNameInData("postcode"), "345 34");
		assertEquals(organisation.getFirstAtomicValueWithNameInData("country"), "FI");

	}

	@Test
	public void testOrganisationNumberMissing() {
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("organisationNumber"));
	}

	@Test
	public void testOrganisationNumberIsnull() {
		rowFromDb.put("orgnumber", null);
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("organisationNumber"));
	}

	@Test
	public void testOrganisationNumberIsEmpty() {
		rowFromDb.put("orgnumber", "");
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("organisationNumber"));
	}

	@Test
	public void testOrganisationNumber() {
		rowFromDb.put("orgnumber", "540002");
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getFirstAtomicValueWithNameInData("organisationNumber"),
				"540002");
	}

	@Test
	public void testOrganisationCodeMissing() {
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("organisationCode"));
	}

	@Test
	public void testOrganisationCodeIsNull() {
		rowFromDb.put("organisation_code", null);
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("organisationCode"));
	}

	@Test
	public void testOrganisationCodeIsEmpty() {
		rowFromDb.put("organisation_code", "");
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("organisationCode"));
	}

	@Test
	public void testOrganisationCode() {
		rowFromDb.put("organisation_code", "56783545");
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getFirstAtomicValueWithNameInData("organisationCode"),
				"56783545");
	}

	@Test
	public void testOrganisationUrlMissing() {
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("URL"));
	}

	@Test
	public void testOrganisationUrlIsNull() {
		rowFromDb.put("organisation_homepage", null);
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("URL"));
	}

	@Test
	public void testOrganisationUrlIsEmpty() {
		rowFromDb.put("organisation_homepage", "");
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertFalse(organisation.containsChildWithNameInData("URL"));
	}

	@Test
	public void testOrganisationURL() {
		rowFromDb.put("organisation_homepage", "www.something.org");
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertEquals(organisation.getFirstAtomicValueWithNameInData("URL"), "www.something.org");
	}

	// @Test
	// public void testParentIdMissing() {
	// DataGroup organisation = converter.fromMap(rowFromDb);
	// assertFalse(organisation.containsChildWithNameInData("parentOrganisation"));
	// }
	//
	// @Test
	// public void testParentIdIsNull() {
	// rowFromDb.put("organisation_parentid", null);
	// DataGroup organisation = converter.fromMap(rowFromDb);
	// assertFalse(organisation.containsChildWithNameInData("parentOrganisation"));
	// }
	//
	// @Test
	// public void testParentIdIsEmpty() {
	// rowFromDb.put("organisation_parentid", "");
	// DataGroup organisation = converter.fromMap(rowFromDb);
	// assertFalse(organisation.containsChildWithNameInData("parentOrganisation"));
	// }
	//
	// @Test
	// public void testParentId() {
	// rowFromDb.put("organisation_parentid", "someParentOrganisation");
	// DataGroup organisation = converter.fromMap(rowFromDb);
	// DataGroup parentOrg =
	// organisation.getFirstGroupWithNameInData("parentOrganisation");
	// DataGroup parentOrgLink =
	// parentOrg.getFirstGroupWithNameInData("organisationLink");
	// assertEquals(parentOrgLink.getFirstAtomicValueWithNameInData("linkedRecordType"),
	// "divaOrganisation");
	// assertEquals(parentOrgLink.getFirstAtomicValueWithNameInData("linkedRecordId"),
	// "someParentOrganisation");
	// }
}
