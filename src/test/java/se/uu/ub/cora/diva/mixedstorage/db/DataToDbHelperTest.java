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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.testng.annotations.Test;

import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;

public class DataToDbHelperTest {

	@Test
	public void testPrivateConstructor() throws Exception {
		Constructor<DataToDbHelper> constructor = DataToDbHelper.class.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
	}

	@Test(expectedExceptions = InvocationTargetException.class)
	public void testPrivateConstructorInvoke() throws Exception {
		Constructor<DataToDbHelper> constructor = DataToDbHelper.class.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	public void testExtractIdFromDataGroup() {
		DataGroupSpy dataGroup = createDataGroupWithId("2345");

		String id = DataToDbHelper.extractIdFromDataGroup(dataGroup);
		assertEquals(id, "2345");

	}

	private DataGroupSpy createDataGroupWithId(String recordId) {
		DataGroupSpy dataGroup = new DataGroupSpy("someNameInData");
		DataGroupSpy recordInfo = new DataGroupSpy("recordInfo");
		recordInfo.addChild(new DataAtomicSpy("id", recordId));
		dataGroup.addChild(recordInfo);
		return dataGroup;
	}

	@Test(expectedExceptions = DbException.class)
	public void testThrowErrorIfIdNotAnIntWhneIdNotInt() {
		DataToDbHelper.throwDbExceptionIfIdNotAnIntegerValue("notAnInt");
	}

	@Test
	public void testThrowErrorIfIdNotAnIntWhenInt() {
		boolean exeptionCaught = false;
		try {
			DataToDbHelper.throwDbExceptionIfIdNotAnIntegerValue("2345");
		} catch (DbException e) {
			exeptionCaught = true;
		}
		assertFalse(exeptionCaught);
	}
}
