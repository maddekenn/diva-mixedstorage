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
import se.uu.ub.cora.sqldatabase.RecordCreator;
import se.uu.ub.cora.sqldatabase.RecordDeleter;
import se.uu.ub.cora.sqldatabase.RecordReader;

public class RelatedTableFactoryImp implements RelatedTableFactory {

	private RecordReader recordReader;
	private RecordDeleter recordDeleter;
	private RecordCreator recordCreator;

	public static RelatedTableFactoryImp usingReaderUpdaterAndCreator(RecordReader recordReader,
			RecordDeleter recordDeleter, RecordCreator recordCreator) {
		return new RelatedTableFactoryImp(recordReader, recordDeleter, recordCreator);
	}

	private RelatedTableFactoryImp(RecordReader recordReader, RecordDeleter recordDeleter,
			RecordCreator recordCreator) {
		this.recordReader = recordReader;
		this.recordDeleter = recordDeleter;
		this.recordCreator = recordCreator;
	}

	@Override
	public RelatedTable factor(String relatedTableName) {
		if ("organisationAlternativeName".equals(relatedTableName)) {
			return new OrganisationAlternativeNameRelatedTable(recordReader, recordDeleter,
					recordCreator);
		}
		throw NotImplementedException
				.withMessage("Related table not implemented for " + relatedTableName);
	}

}
