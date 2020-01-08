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
package se.uu.ub.cora.diva.mixedstorage.db.organisation;

import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTable;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTableFactory;
import se.uu.ub.cora.sqldatabase.RecordCreator;
import se.uu.ub.cora.sqldatabase.RecordCreatorFactory;
import se.uu.ub.cora.sqldatabase.RecordDeleter;
import se.uu.ub.cora.sqldatabase.RecordDeleterFactory;
import se.uu.ub.cora.sqldatabase.RecordReader;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;

public class RelatedTableFactoryImp implements RelatedTableFactory {

	private RecordReaderFactory recordReaderFactory;
	private RecordDeleterFactory recordDeleterFactory;
	private RecordCreatorFactory recordCreatorFactory;

	public static RelatedTableFactoryImp usingReaderDeleterAndCreator(
			RecordReaderFactory recordReaderFactory, RecordDeleterFactory recordDeleterFactory,
			RecordCreatorFactory recordCreatorFactory) {
		return new RelatedTableFactoryImp(recordReaderFactory, recordDeleterFactory,
				recordCreatorFactory);
	}

	private RelatedTableFactoryImp(RecordReaderFactory recordReader,
			RecordDeleterFactory recordDeleter, RecordCreatorFactory recordCreator) {
		this.recordReaderFactory = recordReader;
		this.recordDeleterFactory = recordDeleter;
		this.recordCreatorFactory = recordCreator;
	}

	@Override
	public RelatedTable factor(String relatedTableName) {
		RecordReader recordReader = recordReaderFactory.factor();
		RecordDeleter recordDeleter = recordDeleterFactory.factor();
		RecordCreator recordCreator = recordCreatorFactory.factor();
		if ("organisationAlternativeName".equals(relatedTableName)) {
			return new OrganisationAlternativeNameRelatedTable(recordReader, recordDeleter,
					recordCreator);
		}
		throw NotImplementedException
				.withMessage("Related table not implemented for " + relatedTableName);
	}

	public RecordReaderFactory getRecordReaderFactory() {
		return recordReaderFactory;
	}

	public RecordDeleterFactory getRecordDeleterFactory() {
		return recordDeleterFactory;
	}

	public RecordCreatorFactory getRecordCreatorFactory() {
		return recordCreatorFactory;
	}

}
