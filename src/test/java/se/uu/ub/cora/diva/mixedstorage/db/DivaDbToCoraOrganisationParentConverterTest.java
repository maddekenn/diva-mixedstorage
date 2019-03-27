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
import static org.testng.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.bookkeeper.data.DataGroup;

public class DivaDbToCoraOrganisationParentConverterTest {
	private DivaDbToCoraOrganisationParentConverter converter;
	private Map<String, String> rowFromDb;

	@BeforeMethod
	public void beforeMethod() {
		rowFromDb = new HashMap<>();
		rowFromDb.put("organisation_id", "someOrgId");
		rowFromDb.put("organisation_parent_id", "someParentId");
		converter = new DivaDbToCoraOrganisationParentConverter();

	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation parent to Cora organisation parent: Map does not contain mandatory values for organisation id and parent id")
	public void testEmptyMap() {
		rowFromDb = new HashMap<>();
		DataGroup organisation = converter.fromMap(rowFromDb);
		assertNull(organisation);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation parent to Cora organisation parent: "
			+ "Map does not contain mandatory values for organisation id and parent id")
	public void testMapWithEmptyValueForOrganisationIdThrowsError() {
		rowFromDb = new HashMap<>();
		rowFromDb.put("organisation_id", "");
		converter.fromMap(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation parent to Cora organisation parent:"
			+ " Map does not contain mandatory values for organisation id and parent id")
	public void testMapWithMissingParentIdThrowsError() {
		rowFromDb = new HashMap<>();
		rowFromDb.put("organisation_id", "someOrgId");
		converter.fromMap(rowFromDb);
	}

	@Test(expectedExceptions = ConversionException.class, expectedExceptionsMessageRegExp = ""
			+ "Error converting organisation parent to Cora organisation parent: "
			+ "Map does not contain mandatory values for organisation id and parent id")
	public void testMapWithEmptyValueForParentIdThrowsError() {
		rowFromDb = new HashMap<>();
		rowFromDb.put("organisation_id", "someOrgId");
		rowFromDb.put("organisation_parent_id", "");
		converter.fromMap(rowFromDb);
	}

	@Test
	public void testMinimalValuesReturnsDataGroupWithCorrectStructure() {
		DataGroup parent = converter.fromMap(rowFromDb);
		assertEquals(parent.getNameInData(), "parentOrganisation");
		DataGroup linkedOrganisation = parent.getFirstGroupWithNameInData("organisationLink");

		assertEquals(linkedOrganisation.getFirstAtomicValueWithNameInData("linkedRecordType"),
				"divaOrganisation");
		assertEquals(linkedOrganisation.getFirstAtomicValueWithNameInData("linkedRecordId"),
				"someParentId");
	}

}
