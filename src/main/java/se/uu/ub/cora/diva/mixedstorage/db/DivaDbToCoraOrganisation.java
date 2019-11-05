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

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.sqldatabase.RecordReader;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;

public class DivaDbToCoraOrganisation implements DivaDbToCora {

	private static final String DIVA_ORGANISATION_PREDECESSOR = "divaOrganisationPredecessor";
	private static final String CLOSED_DATE = "closed_date";
	private RecordReaderFactory recordReaderFactory;
	private DivaDbToCoraConverterFactory converterFactory;
	private RecordReader recordReader;
	private String organisationClosedDate = null;

	public DivaDbToCoraOrganisation(RecordReaderFactory recordReaderFactory,
			DivaDbToCoraConverterFactory converterFactory) {
		this.recordReaderFactory = recordReaderFactory;
		this.converterFactory = converterFactory;
	}

	public static DivaDbToCoraOrganisation usingRecordReaderFactoryAndConverterFactory(
			RecordReaderFactory recordReaderFactory,
			DivaDbToCoraConverterFactory converterFactory) {
		return new DivaDbToCoraOrganisation(recordReaderFactory, converterFactory);
	}

	@Override
	public DataGroup convertOneRowData(String type, String id) {
		recordReader = getRecordReaderFactory().factor();
		DataGroup organisation = readAndConvertOrganisationFromDb(type, id);
		tryToReadAndConvertParents(id, organisation);
		tryToReadAndConvertPredecessors(id, organisation);
		tryToReadAndConvertSuccessors(id, organisation);
		return organisation;
	}

	private DataGroup readAndConvertOrganisationFromDb(String type, String id) {
		Map<String, Object> readRow = readOneRowFromDbUsingTypeAndId(type, id);
		saveClosedDateIfItExists(readRow);
		return convertOneMapFromDbToDataGroup(type, readRow);
	}

	private Map<String, Object> readOneRowFromDbUsingTypeAndId(String type, String id) {
		Map<String, Object> conditions = new HashMap<>();
		conditions.put("id", id);
		return recordReader.readOneRowFromDbUsingTableAndConditions(type, conditions);
	}

	private void saveClosedDateIfItExists(Map<String, Object> readRow) {
		Object closedDate = readRow.get(CLOSED_DATE);
		if (closedDate != null && !"".equals(closedDate)) {
			organisationClosedDate = getDateAsString(readRow);
		}
	}

	private String getDateAsString(Map<String, Object> readRow) {
		Date closedDate = (Date) readRow.get(CLOSED_DATE);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat.format(closedDate);
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

	private void tryToReadAndConvertPredecessors(String id, DataGroup organisation) {
		Map<String, Object> conditions = new HashMap<>();
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

	private void tryToReadAndConvertSuccessors(String id, DataGroup organisation) {
		Map<String, Object> conditions = new HashMap<>();
		conditions.put("predecessor_id", id);
		List<Map<String, Object>> successors = recordReader
				.readFromTableUsingConditions(DIVA_ORGANISATION_PREDECESSOR, conditions);

		possiblyConvertSuccessors(organisation, successors);
	}

	private void possiblyConvertSuccessors(DataGroup organisation,
			List<Map<String, Object>> successors) {
		if (collectionContainsData(successors)) {
			convertAndAddSuccessors(organisation, successors);
		}
	}

	private void convertAndAddSuccessors(DataGroup organisation,
			List<Map<String, Object>> successors) {
		int repeatId = 0;
		for (Map<String, Object> successorsValues : successors) {
			addClosedDateToSuccessorIfOrganisationHasClosedDate(successorsValues);
			convertAndAddSuccessor(organisation, repeatId, successorsValues);
			repeatId++;
		}
	}

	private void addClosedDateToSuccessorIfOrganisationHasClosedDate(
			Map<String, Object> successorsValues) {
		if (organisationClosedDate != null) {
			successorsValues.put(CLOSED_DATE, organisationClosedDate);
		}
	}

	private void convertAndAddSuccessor(DataGroup organisation, int repeatId,
			Map<String, Object> successorsValues) {
		DivaDbToCoraConverter successorsConverter = getConverterFactory()
				.factor("divaOrganisationSuccessor");
		DataGroup convertedSuccessor = successorsConverter.fromMap(successorsValues);
		convertedSuccessor.setRepeatId(String.valueOf(repeatId));
		organisation.addChild(convertedSuccessor);
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
