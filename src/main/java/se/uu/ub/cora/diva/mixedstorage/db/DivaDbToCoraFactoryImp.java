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

import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.diva.mixedstorage.db.organisation.DivaDbToCoraOrganisation;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;

public class DivaDbToCoraFactoryImp implements DivaDbFactory {

	private RecordReaderFactory readerFactory;
	private DivaDbToCoraConverterFactory converterFactory;

	public DivaDbToCoraFactoryImp(RecordReaderFactory readerFactory,
			DivaDbToCoraConverterFactory converterFactory) {
		this.readerFactory = readerFactory;
		this.converterFactory = converterFactory;
	}

	@Override
	public DivaDb factor(String type) {
		if ("divaOrganisation".equals(type)) {
			return DivaDbToCoraOrganisation
					.usingRecordReaderFactoryAndConverterFactory(readerFactory, converterFactory);
		}
		throw NotImplementedException.withMessage("No implementation found for: " + type);
	}

	public RecordReaderFactory getReaderFactory() {
		// for testing
		return readerFactory;
	}

	public DivaDbToCoraConverterFactory getConverterFactory() {
		// for testing
		return converterFactory;
	}

}
