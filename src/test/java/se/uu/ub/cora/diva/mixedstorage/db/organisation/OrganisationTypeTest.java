/*
 * Copyright 2020 Uppsala University Library
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
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.testng.annotations.Test;

public class OrganisationTypeTest {

	@Test
	public void testPrivateConstructor() throws Exception {
		Constructor<OrganisationType> constructor = OrganisationType.class.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
	}

	@Test(expectedExceptions = InvocationTargetException.class)
	public void testPrivateConstructorInvoke() throws Exception {
		Constructor<OrganisationType> constructor = OrganisationType.class.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	public void testGetTypeCode() {

		assertEquals(OrganisationType.getTypeCodeForIntValue(49), "root");
		assertEquals(OrganisationType.getTypeCodeForIntValue(50), "university");
		assertEquals(OrganisationType.getTypeCodeForIntValue(51), "school");
		assertEquals(OrganisationType.getTypeCodeForIntValue(52), "faculty");
		assertEquals(OrganisationType.getTypeCodeForIntValue(53), "centre");
		assertEquals(OrganisationType.getTypeCodeForIntValue(54), "department");
		assertEquals(OrganisationType.getTypeCodeForIntValue(55), "unit");
		assertEquals(OrganisationType.getTypeCodeForIntValue(56), "section");
		assertEquals(OrganisationType.getTypeCodeForIntValue(57), "domain");
		assertEquals(OrganisationType.getTypeCodeForIntValue(58), "researchGroup");
		assertEquals(OrganisationType.getTypeCodeForIntValue(59), "researchProgram");
		assertEquals(OrganisationType.getTypeCodeForIntValue(60), "researchProject");
		assertEquals(OrganisationType.getTypeCodeForIntValue(61), "division");
		assertEquals(OrganisationType.getTypeCodeForIntValue(62), "museum");
		assertEquals(OrganisationType.getTypeCodeForIntValue(63), "researchInstitute");
		assertEquals(OrganisationType.getTypeCodeForIntValue(64), "authority");
		assertEquals(OrganisationType.getTypeCodeForIntValue(65), "company");
		assertEquals(OrganisationType.getTypeCodeForIntValue(66), "other");

	}
}
