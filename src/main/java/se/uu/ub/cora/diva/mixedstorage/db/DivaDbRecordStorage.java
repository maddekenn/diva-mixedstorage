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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.sqldatabase.RecordReader;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;
import se.uu.ub.cora.sqldatabase.SqlStorageException;
import se.uu.ub.cora.storage.RecordNotFoundException;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.StorageReadResult;

public class DivaDbRecordStorage implements RecordStorage {

	private static final String ORGANISATION = "organisation";
	private static final String DIVA_ORGANISATION = "divaOrganisation";
	private RecordReaderFactory recordReaderFactory;
	private DivaDbToCoraConverterFactory converterFactory;
	private DivaDbFactory divaDbFactory;
	private RecordStorageForOneTypeFactory recordStorageForOneTypeFactory;

	private DivaDbRecordStorage(RecordReaderFactory recordReaderFactory,
			DivaDbToCoraConverterFactory converterFactory, DivaDbFactory divaDbFactory,
			RecordStorageForOneTypeFactory recordStorageForOneTypeFactory) {
		this.recordReaderFactory = recordReaderFactory;
		this.converterFactory = converterFactory;
		this.divaDbFactory = divaDbFactory;
		this.recordStorageForOneTypeFactory = recordStorageForOneTypeFactory;

	}

	public static DivaDbRecordStorage usingRecordReaderFactoryConverterFactoryDbToCoraFactoryAndRecordStorageForOneTypeFactory(
			RecordReaderFactory recordReaderFactory, DivaDbToCoraConverterFactory converterFactory,
			DivaDbFactory divaDbFactory,
			RecordStorageForOneTypeFactory recordStorageForOneTypeFactory) {
		return new DivaDbRecordStorage(recordReaderFactory, converterFactory,
				divaDbFactory, recordStorageForOneTypeFactory);
	}

	@Override
	public DataGroup read(String type, String id) {
		if (DIVA_ORGANISATION.equals(type)) {
			DivaDb divaDbToCora = divaDbFactory.factor(type);
			return divaDbToCora.read(type, id);
		}
		throw NotImplementedException.withMessage("read is not implemented for type: " + type);
	}

	@Override
	public void create(String type, String id, DataGroup record, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		throw NotImplementedException.withMessage("create is not implemented");
	}

	@Override
	public void deleteByTypeAndId(String type, String id) {
		throw NotImplementedException.withMessage("deleteByTypeAndId is not implemented");
	}

	@Override
	public boolean linksExistForRecord(String type, String id) {
		throw NotImplementedException.withMessage("linksExistForRecord is not implemented");
	}

	@Override
	public void update(String type, String id, DataGroup record, DataGroup collectedTerms,
			DataGroup linkList, String dataDivider) {
		if (DIVA_ORGANISATION.equals(type)) {
			updateOrganisation(record);
		} else {
			throw NotImplementedException.withMessage("update is not implemented");
		}
	}

	private void updateOrganisation(DataGroup dataGroup) {
		RecordStorageForOneType recordStorageForOneType = recordStorageForOneTypeFactory
				.factor(ORGANISATION);
		recordStorageForOneType.update(dataGroup);
	}

	private Map<String, Object> createConditionsAddingOrganisationId(String id) {
		throwDbExceptionIfIdNotAnIntegerValue(id);
		Map<String, Object> conditions = new HashMap<>(1);
		conditions.put("organisation_id", Integer.valueOf(id));
		return conditions;
	}

	private void throwDbExceptionIfIdNotAnIntegerValue(String id) {
		try {
			Integer.valueOf(id);
		} catch (NumberFormatException ne) {
			throw DbException.withMessageAndException("Record not found: " + id, ne);
		}
	}

	@Override
	public StorageReadResult readList(String type, DataGroup filter) {
		if (DIVA_ORGANISATION.equals(type)) {
			List<Map<String, Object>> rowsFromDb = readAllFromDb(type);
			return createStorageReadResultFromDbData(type, rowsFromDb);
		}
		throw NotImplementedException.withMessage("readList is not implemented for type: " + type);
	}

	private List<Map<String, Object>> readAllFromDb(String type) {
		RecordReader recordReader = recordReaderFactory.factor();
		return recordReader.readAllFromTable(type);
	}

	private StorageReadResult createStorageReadResultFromDbData(String type,
			List<Map<String, Object>> rowsFromDb) {
		StorageReadResult storageReadResult = new StorageReadResult();
		storageReadResult.listOfDataGroups = convertListOfMapsFromDbToDataGroups(type, rowsFromDb);
		return storageReadResult;
	}

	private List<DataGroup> convertListOfMapsFromDbToDataGroups(String type,
			List<Map<String, Object>> readAllFromTable) {
		List<DataGroup> convertedList = new ArrayList<>(readAllFromTable.size());
		for (Map<String, Object> map : readAllFromTable) {
			DataGroup convertedGroup = convertOneMapFromDbToDataGroup(type, map);
			convertedList.add(convertedGroup);
		}
		return convertedList;
	}

	private DataGroup convertOneMapFromDbToDataGroup(String type, Map<String, Object> readRow) {
		DivaDbToCoraConverter dbToCoraConverter = converterFactory.factor(type);
		return dbToCoraConverter.fromMap(readRow);
	}

	@Override
	public StorageReadResult readAbstractList(String type, DataGroup filter) {
		throw NotImplementedException.withMessage("readAbstractList is not implemented");
	}

	@Override
	public DataGroup readLinkList(String type, String id) {
		throw NotImplementedException.withMessage("readLinkList is not implemented");
	}

	@Override
	public Collection<DataGroup> generateLinkCollectionPointingToRecord(String type, String id) {
		throw NotImplementedException
				.withMessage("generateLinkCollectionPointingToRecord is not implemented");
	}

	@Override
	public boolean recordsExistForRecordType(String type) {
		throw NotImplementedException.withMessage("recordsExistForRecordType is not implemented");
	}

	@Override
	public boolean recordExistsForAbstractOrImplementingRecordTypeAndRecordId(String type,
			String id) {
		if (DIVA_ORGANISATION.equals(type)) {
			return organisationExistsInDb(id);
		}
		throw NotImplementedException.withMessage(
				"recordExistsForAbstractOrImplementingRecordTypeAndRecordId is not implemented");
	}

	private boolean organisationExistsInDb(String id) {
		try {
			tryToReadOrganisationFromDb(id);
			return true;
		} catch (RecordNotFoundException e) {
			return false;
		}
	}

	private Map<String, Object> tryToReadOrganisationFromDb(String id) {
		try {
			RecordReader recordReader = recordReaderFactory.factor();
			Map<String, Object> conditions = createConditionsAddingOrganisationId(id);
			return recordReader.readOneRowFromDbUsingTableAndConditions(ORGANISATION, conditions);
		} catch (SqlStorageException | DbException e) {
			throw new RecordNotFoundException("Organisation not found: " + id);
		}
	}

	public DivaDbToCoraConverterFactory getConverterFactory() {
		// needed for test
		return converterFactory;
	}

	public RecordReaderFactory getRecordReaderFactory() {
		// needed for test
		return recordReaderFactory;
	}

	public DivaDbFactory getDivaDbToCoraFactory() {
		// needed for test
		return divaDbFactory;
	}

	public RecordStorageForOneTypeFactory getRecordStorageForOneTypeFactory() {
		// needed for test
		return recordStorageForOneTypeFactory;
	}

}
