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
package se.uu.ub.cora.diva.mixedstorage.db.organisation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbReader;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverter;
import se.uu.ub.cora.diva.mixedstorage.db.DivaDbToCoraConverterFactory;
import se.uu.ub.cora.sqldatabase.RecordReader;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;

public class DivaDbOrganisationReader implements DivaDbReader {

	private static final String DIVA_ORGANISATION_PREDECESSOR = "divaOrganisationPredecessor";
	private RecordReaderFactory recordReaderFactory;
	private DivaDbToCoraConverterFactory converterFactory;
	private RecordReader recordReader;

	public DivaDbOrganisationReader(RecordReaderFactory recordReaderFactory,
			DivaDbToCoraConverterFactory converterFactory) {
		this.recordReaderFactory = recordReaderFactory;
		this.converterFactory = converterFactory;
	}

	public static DivaDbOrganisationReader usingRecordReaderFactoryAndConverterFactory(
			RecordReaderFactory recordReaderFactory,
			DivaDbToCoraConverterFactory converterFactory) {
		return new DivaDbOrganisationReader(recordReaderFactory, converterFactory);
	}

	@Override
	public DataGroup read(String type, String id) {
		recordReader = getRecordReaderFactory().factor();
		DataGroup organisation = readAndConvertOrganisationFromDb(type, id);
		tryToReadAndConvertParents(id, organisation);
		tryToReadAndConvertPredecessors(id, organisation);
		return organisation;
	}

	private DataGroup readAndConvertOrganisationFromDb(String type, String id) {
		Map<String, Object> readRow = readOneRowFromDbUsingTypeAndId(type, id);
		return convertOneMapFromDbToDataGroup(type, readRow);
	}

	private Map<String, Object> readOneRowFromDbUsingTypeAndId(String type, String id) {
		Map<String, Object> conditions = new HashMap<>();
		conditions.put("id", id);
		return recordReader.readOneRowFromDbUsingTableAndConditions(type, conditions);
	}

	private DataGroup convertOneMapFromDbToDataGroup(String type, Map<String, Object> readRow) {
		DivaDbToCoraConverter dbToCoraConverter = getConverterFactory().factor(type);
		return dbToCoraConverter.fromMap(readRow);
	}

	private void tryToReadAndConvertParents(String id, DataGroup organisation) {
		Map<String, Object> conditions = new HashMap<>();
		conditions.put("organisation_id", id);
		List<Map<String, Object>> parents = recordReader
				.readFromTableUsingConditions("divaOrganisationParent", conditions);

		possiblyConvertParents(organisation, parents);
	}

	private void possiblyConvertParents(DataGroup organisation, List<Map<String, Object>> parents) {
		if (collectionContainsData(parents)) {
			convertAndAddParents(organisation, parents);
		}
	}

	private void convertAndAddParents(DataGroup organisation, List<Map<String, Object>> parents) {
		int repeatId = 0;
		for (Map<String, Object> parentValues : parents) {
			convertAndAddParent(organisation, repeatId, parentValues);
			repeatId++;
		}
	}

	private void convertAndAddParent(DataGroup organisation, int repeatId,
			Map<String, Object> parentValues) {
		DivaDbToCoraConverter predecessorConverter = getConverterFactory()
				.factor("divaOrganisationParent");
		DataGroup parent = predecessorConverter.fromMap(parentValues);
		parent.setRepeatId(String.valueOf(repeatId));
		organisation.addChild(parent);
	}

	private void tryToReadAndConvertPredecessors(String stringId, DataGroup organisation) {
		Map<String, Object> conditions = new HashMap<>();
		int id = Integer.parseInt(stringId);
		conditions.put("organisation_id", id);
		List<Map<String, Object>> predecessors = recordReader
				.readFromTableUsingConditions(DIVA_ORGANISATION_PREDECESSOR, conditions);

		possiblyConvertPredecessors(organisation, predecessors);
	}

	private void possiblyConvertPredecessors(DataGroup organisation,
			List<Map<String, Object>> predecessors) {
		if (collectionContainsData(predecessors)) {
			convertAndAddPredecessors(organisation, predecessors);
		}
	}

	private boolean collectionContainsData(List<Map<String, Object>> successors) {
		return successors != null && !successors.isEmpty();
	}

	private void convertAndAddPredecessors(DataGroup organisation,
			List<Map<String, Object>> predecessors) {
		int repeatId = 0;
		for (Map<String, Object> predecessorValues : predecessors) {
			convertAndAddPredecessor(organisation, repeatId, predecessorValues);
			repeatId++;
		}
	}

	private void convertAndAddPredecessor(DataGroup organisation, int repeatId,
			Map<String, Object> predecessorValues) {
		DivaDbToCoraConverter predecessorConverter = getConverterFactory()
				.factor(DIVA_ORGANISATION_PREDECESSOR);
		DataGroup predecessor = predecessorConverter.fromMap(predecessorValues);
		predecessor.setRepeatId(String.valueOf(repeatId));
		organisation.addChild(predecessor);
	}

	public RecordReaderFactory getRecordReaderFactory() {
		// for testing
		return recordReaderFactory;
	}

	public DivaDbToCoraConverterFactory getConverterFactory() {
		// for testing
		return converterFactory;
	}
}
