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

import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.MultipleRowDbParentReader;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.MultipleRowDbReader;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;

public class MultipleRowDbReaderFactoryImp implements MultipleRowDbReaderFactory {

	private RecordReaderFactory recordReaderFactory;
	private DivaDbToCoraConverterFactory converterFactory;

	public static MultipleRowDbReaderFactoryImp usingReaderFactoryAndConverterFactory(
			RecordReaderFactory recordReaderFactory,
			DivaDbToCoraConverterFactory converterFactory) {
		return new MultipleRowDbReaderFactoryImp(recordReaderFactory, converterFactory);
	}

	private MultipleRowDbReaderFactoryImp(RecordReaderFactory recordReaderFactory,
			DivaDbToCoraConverterFactory converterFactory) {
		this.recordReaderFactory = recordReaderFactory;
		this.converterFactory = converterFactory;
	}

	@Override
	public MultipleRowDbReader factor(String type) {
		if ("divaOrganisationParent".equals(type)) {
			return new MultipleRowDbParentReader(recordReaderFactory, converterFactory);

		}
		throw NotImplementedException.withMessage("No implementation found for: " + type);

	}

	public RecordReaderFactory getRecordReaderFactory() {
		return recordReaderFactory;
	}

	public DivaDbToCoraConverterFactory getConverterFactory() {
		return converterFactory;
	}

}
