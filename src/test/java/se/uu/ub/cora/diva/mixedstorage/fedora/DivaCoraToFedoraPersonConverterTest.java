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
package se.uu.ub.cora.diva.mixedstorage.fedora;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import se.uu.ub.cora.bookkeeper.data.DataAtomic;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.resource.ResourceReader;

public class DivaCoraToFedoraPersonConverterTest {
	@Test
	public void testConvertToFedoraXML() throws Exception {

		HttpHandlerFactorySpy httpHandlerFactory = new HttpHandlerFactorySpy();
		httpHandlerFactory.responseText = ResourceReader.readResourceAsString("person/11685.xml");

		String fedoraURL = "someFedoraURL";
		DivaCoraToFedoraConverter converter = DivaCoraToFedoraPersonConverter
				.usingHttpHandlerFactoryAndFedoraUrl(httpHandlerFactory, fedoraURL);
		DataGroup record = createPerson11685DataGroup();

		String xml = converter.toXML(record);
		assertEquals(httpHandlerFactory.factoredHttpHandlers.size(), 1);
		assertEquals(httpHandlerFactory.urls.get(0),
				fedoraURL + "objects/authority-person:11685/datastreams/METADATA/content");

		HttpHandlerSpy httpHandler = httpHandlerFactory.factoredHttpHandlers.get(0);
		assertEquals(httpHandler.requestMetod, "GET");

		assertEquals(xml, ResourceReader.readResourceAsString("person/expectedUpdated11685.xml"));

	}

	private DataGroup createPerson11685DataGroup() {
		DataGroup record = DataGroup.withNameInData("authorityPerson");
		DataGroup recordInfo = DataGroup.withNameInData("recordInfo");
		record.addChild(recordInfo);
		recordInfo.addChild(DataAtomic.withNameInDataAndValue("id", "authority-person:11685"));

		DataGroup authorizedNameGroup = DataGroup.withNameInData("authorizedName");
		record.addChild(authorizedNameGroup);

		DataAtomic familyName = DataAtomic.withNameInDataAndValue("familyName", "Andersson");
		authorizedNameGroup.addChild(familyName);

		DataAtomic givenName = DataAtomic.withNameInDataAndValue("givenName", "Karl");
		authorizedNameGroup.addChild(givenName);

		return record;
	}

}
