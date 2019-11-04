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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import se.uu.ub.cora.data.DataAtomic;
import se.uu.ub.cora.data.DataGroup;

public class DivaDbToCoraOrganisationConverter implements DivaDbToCoraConverter {

	private static final String ORGANISATION_ID = "id";
	private static final String ALTERNATIVE_NAME = "alternative_name";
	private Map<String, Object> dbRow;
	private DataGroup organisation;

	@Override
	public DataGroup fromMap(Map<String, Object> dbRow) {
		this.dbRow = dbRow;
		if (organisationIsEmpty()) {
			throw ConversionException.withMessageAndException(
					"Error converting organisation to Cora organisation: Map does not contain value for "
							+ ORGANISATION_ID,
					null);
		}
		return createDataGroup();
	}

	private boolean organisationIsEmpty() {
		return !dbRow.containsKey(ORGANISATION_ID) || "".equals(dbRow.get(ORGANISATION_ID));
	}

	private DataGroup createDataGroup() {
		createAndAddOrganisationWithRecordInfo();
		createAndAddName();
		createAndAddAlternativeName();
		createAndAddOrganisationType();
		possiblyCreateAndAddEligibility();
		possiblyCeateAndAddAddress();
		possiblyCreateAndAddOrganisationNumber();
		possiblyCreateAndAddOrganisationCode();
		possiblyCreateAndAddURL();

		return organisation;
	}

	private void createAndAddOrganisationWithRecordInfo() {
		organisation = DataGroup.withNameInData("organisation");
		String id = (String) dbRow.get(ORGANISATION_ID);
		DataGroup recordInfo = createRecordInfo(id);
		organisation.addChild(recordInfo);
	}

	private DataGroup createRecordInfo(String id) {
		DataGroup recordInfo = DataGroup.withNameInData("recordInfo");
		recordInfo.addChild(DataAtomic.withNameInDataAndValue("id", id));
		createAndAddType(recordInfo);
		createAndAddDataDivider(recordInfo);
		createAndAddCreatedAndUpdatedInfo(recordInfo);
		return recordInfo;
	}

	private void createAndAddType(DataGroup recordInfo) {
		DataGroup type = createLinkUsingNameInDataRecordTypeAndRecordId("type", "recordType",
				"divaOrganisation");
		recordInfo.addChild(type);
	}

	private DataGroup createLinkUsingNameInDataRecordTypeAndRecordId(String nameInData,
			String linkedRecordType, String linkedRecordId) {
		DataGroup linkGroup = DataGroup.withNameInData(nameInData);
		linkGroup.addChild(DataAtomic.withNameInDataAndValue("linkedRecordType", linkedRecordType));
		linkGroup.addChild(DataAtomic.withNameInDataAndValue("linkedRecordId", linkedRecordId));
		return linkGroup;
	}

	private void createAndAddDataDivider(DataGroup recordInfo) {
		DataGroup dataDivider = createLinkUsingNameInDataRecordTypeAndRecordId("dataDivider",
				"system", "diva");
		recordInfo.addChild(dataDivider);
	}

	private void createAndAddCreatedAndUpdatedInfo(DataGroup recordInfo) {
		createAndAddCreatedInfo(recordInfo);

		createAndAddUpdatedInfo(recordInfo);
	}

	private void createAndAddCreatedInfo(DataGroup recordInfo) {
		DataGroup createdBy = createLinkUsingNameInDataRecordTypeAndRecordId("createdBy",
				"coraUser", "coraUser:4412982402853626");
		recordInfo.addChild(createdBy);
		addPredefinedTimestampToDataGroupUsingNameInData(recordInfo, "tsCreated");
	}

	private void createAndAddUpdatedInfo(DataGroup recordInfo) {
		DataGroup updated = DataGroup.withNameInData("updated");
		DataGroup updatedBy = createLinkUsingNameInDataRecordTypeAndRecordId("updatedBy",
				"coraUser", "coraUser:4412982402853626");
		updatedBy.setRepeatId("0");
		updated.addChild(updatedBy);
		addPredefinedTimestampToDataGroupUsingNameInData(updated, "tsUpdated");
		recordInfo.addChild(updated);
	}

	private void addPredefinedTimestampToDataGroupUsingNameInData(DataGroup recordInfo,
			String nameInData) {
		LocalDateTime tsCreated = LocalDateTime.of(2015, 01, 01, 00, 00, 00);
		String dateTimeString = getLocalTimeDateAsString(tsCreated);
		recordInfo.addChild(DataAtomic.withNameInDataAndValue(nameInData, dateTimeString));
	}

	private String getLocalTimeDateAsString(LocalDateTime localDateTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		return localDateTime.format(formatter);
	}

	private void createAndAddName() {
		String divaOrganisationName = (String) dbRow.get("defaultname");
		organisation.addChild(
				DataAtomic.withNameInDataAndValue("organisationName", divaOrganisationName));
	}

	private void createAndAddAlternativeName() {
		DataGroup alternativeNameDataGroup = DataGroup.withNameInData("alternativeName");
		alternativeNameDataGroup.addChild(DataAtomic.withNameInDataAndValue("language", "en"));
		String alternativeName = (String) dbRow.get(ALTERNATIVE_NAME);
		alternativeNameDataGroup
				.addChild(DataAtomic.withNameInDataAndValue("organisationName", alternativeName));
		organisation.addChild(alternativeNameDataGroup);
	}

	private void createAndAddOrganisationType() {
		organisation.addChild(DataAtomic.withNameInDataAndValue("organisationType", "unit"));
	}

	private void possiblyCreateAndAddEligibility() {
		Object notEligable = dbRow.get("not_eligible");
		if (notEligable != null) {
			createAndAddEligibility(notEligable);

		}
	}

	private void createAndAddEligibility(Object notEligable) {
		String coraEligible = isEligible(notEligable) ? "yes" : "no";
		organisation.addChild(DataAtomic.withNameInDataAndValue("eligible", coraEligible));
	}

	private boolean isEligible(Object notEligable) {
		return !(boolean) notEligable;
	}

	private void possiblyCeateAndAddAddress() {
		possiblyAddAtomicValueUsingKeyAndNameInData("city", "city");
		possiblyAddAtomicValueUsingKeyAndNameInData("street", "street");
		possiblyAddAtomicValueUsingKeyAndNameInData("box", "box");
		possiblyAddAtomicValueUsingKeyAndNameInData("postnumber", "postcode");
		possiblyAddCountryConvertedToUpperCase();
	}

	private void possiblyAddAtomicValueUsingKeyAndNameInData(String key, String nameInData) {
		if (valueExistsForKey(key)) {
			String value = (String) dbRow.get(key);
			organisation.addChild(DataAtomic.withNameInDataAndValue(nameInData, value));
		}
	}

	private boolean valueExistsForKey(String key) {
		return dbRow.containsKey(key) && valueForKeyHoldsNonEmptyData(key);
	}

	private boolean valueForKeyHoldsNonEmptyData(String key) {
		return dbRow.get(key) != null && !"".equals(dbRow.get(key));
	}

	private void possiblyAddCountryConvertedToUpperCase() {
		if (valueExistsForKey("country_code")) {
			String uppercaseValue = ((String) dbRow.get("country_code")).toUpperCase();
			organisation.addChild(DataAtomic.withNameInDataAndValue("country", uppercaseValue));
		}
	}

	private void possiblyCreateAndAddOrganisationNumber() {
		possiblyAddAtomicValueUsingKeyAndNameInData("orgnumber", "organisationNumber");
	}

	private void possiblyCreateAndAddOrganisationCode() {
		possiblyAddAtomicValueUsingKeyAndNameInData("organisation_code", "organisationCode");
	}

	private void possiblyCreateAndAddURL() {
		possiblyAddAtomicValueUsingKeyAndNameInData("organisation_homepage", "URL");
	}

}