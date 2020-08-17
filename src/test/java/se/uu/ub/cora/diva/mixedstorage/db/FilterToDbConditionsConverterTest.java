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
package se.uu.ub.cora.diva.mixedstorage.db;

import static org.testng.Assert.assertEquals;

import java.util.Map;

import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataAtomicSpy;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;

public class FilterToDbConditionsConverterTest {

	@Test
	public void testOneCondition() {
		FilterToDbConditionsConverter filterConverter = new FilterToDbConditionsConverterImp();

		DataGroup filter = createFilter();
		Map<String, Object> conditions = filterConverter.convert(filter);
		assertEquals(conditions.get("domain"), "test");
	}

	@Test
	public void testTwoConditions() {
		FilterToDbConditionsConverter filterConverter = new FilterToDbConditionsConverterImp();

		DataGroupSpy filter = createFilter();
		DataGroupSpy part = createPartUsingKeyValueAndRepeatId("someOtherKey", "someOtherValue",
				"1");
		filter.addChild(part);

		Map<String, Object> conditions = filterConverter.convert(filter);
		assertEquals(conditions.get("domain"), "test");
		assertEquals(conditions.get("someOtherKey"), "someOtherValue");
	}

	private DataGroupSpy createFilter() {
		DataGroupSpy filter = new DataGroupSpy("filter");
		DataGroupSpy part = createPartUsingKeyValueAndRepeatId("domain", "test", "0");
		filter.addChild(part);
		return filter;
	}

	private DataGroupSpy createPartUsingKeyValueAndRepeatId(String key, String value,
			String repeatId) {
		DataGroupSpy part = new DataGroupSpy("part");
		part.addChild(new DataAtomicSpy("key", key));
		part.addChild(new DataAtomicSpy("value", value));
		part.setRepeatId(repeatId);
		return part;
	}

	// TODO: hur hantera annat än strängar??

}
