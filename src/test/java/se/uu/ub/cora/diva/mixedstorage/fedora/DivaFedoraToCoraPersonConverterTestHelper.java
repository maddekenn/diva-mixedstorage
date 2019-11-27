package se.uu.ub.cora.diva.mixedstorage.fedora;

import static org.testng.Assert.assertEquals;

import java.util.Collection;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataAttributeSpy;

public class DivaFedoraToCoraPersonConverterTestHelper {
	public static void assertCorrectIdUsingRecordInfoAndId(DataGroup recordInfo, String id) {
		assertEquals(recordInfo.getFirstAtomicValueWithNameInData("id"), id);
	}

	public static Collection<DataGroup> getAlternativeNamesFromPersonDataGroup(
			DataGroup personDataGroup) {
		return personDataGroup.getAllGroupsWithNameInDataAndAttributes("name",
				new DataAttributeSpy("type", "alternative"));
	}

	public static DataGroup getDefaultNameFromPersonDataGroup(DataGroup personDataGroup) {
		return personDataGroup.getAllGroupsWithNameInDataAndAttributes("name",
				new DataAttributeSpy("type", "authorized")).iterator().next();
	}

	public static void assertCorrectTsUpdatedUsingUpdatedAndTsUpdated(DataGroup updatedGroup,
			String tsUpdated) {
		assertEquals(updatedGroup.getFirstAtomicValueWithNameInData("tsUpdated"), tsUpdated);
	}

	public static void assertCorrectTsCreatedUsingRecordInfoAndTsCreated(DataGroup recordInfo,
			String tsCreated) {
		assertEquals(recordInfo.getFirstAtomicValueWithNameInData("tsCreated"), tsCreated);
	}

	public static void assertCorrectUpdatedByUsingUpdatedAndUserId(DataGroup updatedGroup,
			String userId) {
		assertCorrectUserLinkUsingRecordInfoAndNameInDataAndUserId(updatedGroup, "updatedBy",
				userId);
	}

	public static void assertCorrectCreatedByUsingRecordInfoAndUserId(DataGroup recordInfo,
			String userId) {
		assertCorrectUserLinkUsingRecordInfoAndNameInDataAndUserId(recordInfo, "createdBy", userId);
	}

	private static void assertCorrectUserLinkUsingRecordInfoAndNameInDataAndUserId(
			DataGroup recordInfo, String nameInData, String userId) {
		DataGroup createdBy = recordInfo.getFirstGroupWithNameInData(nameInData);
		assertEquals(createdBy.getFirstAtomicValueWithNameInData("linkedRecordType"), "user");
		assertEquals(createdBy.getFirstAtomicValueWithNameInData("linkedRecordId"), userId);
	}

	public static void assertCorrectName(DataGroup defaultName, String givenNameValue,
			String familyNameValue, String additionValue, String numberValue, String repeatId) {
		assertEquals(defaultName.getRepeatId(), repeatId);
		assertCorrectNamePartUsingNameGroupAndAttributeNameAndValue(defaultName, "givenName",
				givenNameValue);
		assertCorrectNamePartUsingNameGroupAndAttributeNameAndValue(defaultName, "familyName",
				familyNameValue);
		assertCorrectNamePartUsingNameGroupAndAttributeNameAndValue(defaultName, "addition",
				additionValue);
		assertCorrectNamePartUsingNameGroupAndAttributeNameAndValue(defaultName, "number",
				numberValue);
	}

	private static void assertCorrectNamePartUsingNameGroupAndAttributeNameAndValue(DataGroup name,
			String attributeName, String nameValue) {
		Collection<DataGroup> givenNames = name.getAllGroupsWithNameInDataAndAttributes("namePart",
				new DataAttributeSpy("type", attributeName));
		if (nameValue == null) {
			assertEquals(givenNames.size(), 0);
		} else {
			assertEquals(givenNames.size(), 1);
			DataGroup givenName = givenNames.iterator().next();
			assertEquals(givenName.getFirstAtomicValueWithNameInData("value"), nameValue);
		}
	}

	public static void assertRecordInfoPersonInDiva(DataGroup recordInfo) {
		DataGroup type = recordInfo.getFirstGroupWithNameInData("type");
		assertEquals(type.getFirstAtomicValueWithNameInData("linkedRecordType"), "recordType");
		assertEquals(type.getFirstAtomicValueWithNameInData("linkedRecordId"), "person");

		DataGroup dataDivider = recordInfo.getFirstGroupWithNameInData("dataDivider");
		assertEquals(dataDivider.getFirstAtomicValueWithNameInData("linkedRecordType"), "system");
		assertEquals(dataDivider.getFirstAtomicValueWithNameInData("linkedRecordId"), "diva");
	}
}
