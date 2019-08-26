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
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.spider.record.storage.RecordNotFoundException;
import se.uu.ub.cora.sqldatabase.DataReader;
import se.uu.ub.cora.sqldatabase.RecordReader;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;
import se.uu.ub.cora.sqldatabase.SqlStorageException;
import se.uu.ub.cora.storage.RecordStorage;
import se.uu.ub.cora.storage.SpiderReadResult;

public class DivaDbToCoraRecordStorage implements RecordStorage {

	private static final String DIVA_ORGANISATION = "divaOrganisation";
	private RecordReaderFactory recordReaderFactory;
	private DivaDbToCoraConverterFactory converterFactory;
	private DivaDbToCoraFactory divaDbToCoraFactory;
	private DataReader dataReader;

	private DivaDbToCoraRecordStorage(RecordReaderFactory recordReaderFactory,
			DivaDbToCoraConverterFactory converterFactory, DivaDbToCoraFactory divaDbToCoraFactory,
			DataReader dataReader) {
		this.recordReaderFactory = recordReaderFactory;
		this.converterFactory = converterFactory;
		this.divaDbToCoraFactory = divaDbToCoraFactory;
		this.dataReader = dataReader;
	}

	public static DivaDbToCoraRecordStorage usingRecordReaderFactoryConverterFactoryAndDbToCoraFactoryAndDataReader(
			RecordReaderFactory recordReaderFactory, DivaDbToCoraConverterFactory converterFactory,
			DivaDbToCoraFactory divaDbToCoraFactory, DataReader dataReader) {
		return new DivaDbToCoraRecordStorage(recordReaderFactory, converterFactory,
				divaDbToCoraFactory, dataReader);
	}

	@Override
	public DataGroup read(String type, String id) {
		if (DIVA_ORGANISATION.equals(type)) {
			DivaDbToCora divaDbToCora = divaDbToCoraFactory.factor(type);
			return divaDbToCora.convertOneRowData(type, id);
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
		throw NotImplementedException.withMessage("update is not implemented");

	}

	@Override
	public SpiderReadResult readList(String type, DataGroup filter) {
		if (DIVA_ORGANISATION.equals(type)) {
			List<Map<String, String>> rowsFromDb = readAllFromDb(type);
			return createSpiderReadResultFromDbData(type, rowsFromDb);
		}
		throw NotImplementedException.withMessage("readList is not implemented for type: " + type);
	}

	private List<Map<String, String>> readAllFromDb(String type) {
		RecordReader recordReader = recordReaderFactory.factor();
		return recordReader.readAllFromTable(type);
	}

	private SpiderReadResult createSpiderReadResultFromDbData(String type,
			List<Map<String, String>> rowsFromDb) {
		SpiderReadResult spiderReadResult = new SpiderReadResult();
		spiderReadResult.listOfDataGroups = convertListOfMapsFromDbToDataGroups(type, rowsFromDb);
		return spiderReadResult;
	}

	private List<DataGroup> convertListOfMapsFromDbToDataGroups(String type,
			List<Map<String, String>> readAllFromTable) {
		List<DataGroup> convertedList = new ArrayList<>();
		for (Map<String, String> map : readAllFromTable) {
			DataGroup convertedGroup = convertOneMapFromDbToDataGroup(type, map);
			convertedList.add(convertedGroup);
		}
		return convertedList;
	}

	private DataGroup convertOneMapFromDbToDataGroup(String type, Map<String, String> readRow) {
		DivaDbToCoraConverter dbToCoraConverter = converterFactory.factor(type);
		return dbToCoraConverter.fromMap(readRow);
	}

	@Override
	public SpiderReadResult readAbstractList(String type, DataGroup filter) {
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
			List<Object> values = createListOfValuesWithId(id);
			return dataReader.readOneRowOrFailUsingSqlAndValues(
					"select * from organisation where organisation_id = ?", values);
		} catch (SqlStorageException e) {
			throw new RecordNotFoundException("Organisation not found: " + id);
		}
	}

	private List<Object> createListOfValuesWithId(String id) {
		throwErrorIfIdNotAnIntegerValue(id);
		Integer idAsInteger = Integer.valueOf(id);
		List<Object> values = new ArrayList<>();
		values.add(idAsInteger);
		return values;
	}

	private void throwErrorIfIdNotAnIntegerValue(String id) {
		try {
			Integer.valueOf(id);
		} catch (NumberFormatException ne) {
			throw new RecordNotFoundException("User not found: " + id);
		}
	}

	public DataReader getDataReader() {
		// needed for test
		return dataReader;
	}

	public DivaDbToCoraConverterFactory getConverterFactory() {
		// needed for test
		return converterFactory;
	}

	public RecordReaderFactory getRecordReaderFactory() {
		// needed for test
		return recordReaderFactory;
	}

	public DivaDbToCoraFactory getDivaDbToCoraFactory() {
		// needed for test
		return divaDbToCoraFactory;
	}

}
