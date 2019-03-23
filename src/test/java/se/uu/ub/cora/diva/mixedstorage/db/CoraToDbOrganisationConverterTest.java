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

import org.testng.annotations.Test;

import se.uu.ub.cora.bookkeeper.data.DataAtomic;
import se.uu.ub.cora.bookkeeper.data.DataGroup;

public class CoraToDbOrganisationConverterTest {

	@Test
	public void updateName() {
		CoraToDbConverter converter = new CoraToDbOrganisationConverter();
		DataGroup dataGroup = createDataGroup();
		PreparedStatementInfo psInfo = converter.convert(dataGroup);

		assertEquals(psInfo.values.size(), 1);
		assertEquals(psInfo.values.get("organisation_name"), "someNewName");

		assertEquals(psInfo.conditions.size(), 1);
		assertEquals(psInfo.conditions.get("organisation_id"), 145);

		assertEquals(psInfo.tableName, "organisation");
	}

	private DataGroup createDataGroup() {
		DataGroup dataGroup = DataGroup.withNameInData("organsation");
		DataGroup recordInfo = DataGroup.withNameInData("recordInfo");
		recordInfo.addChild(DataAtomic.withNameInDataAndValue("id", "145"));
		dataGroup.addChild(recordInfo);
		dataGroup.addChild(DataAtomic.withNameInDataAndValue("organisationName", "someNewName"));
		return dataGroup;
	}

}
