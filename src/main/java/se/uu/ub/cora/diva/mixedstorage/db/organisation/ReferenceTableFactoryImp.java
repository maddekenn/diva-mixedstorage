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
import se.uu.ub.cora.sqldatabase.RecordCreatorFactory;
import se.uu.ub.cora.sqldatabase.RecordDeleter;
import se.uu.ub.cora.sqldatabase.RecordDeleterFactory;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;
import se.uu.ub.cora.sqldatabase.RecordUpdaterFactory;

public class ReferenceTableFactoryImp implements ReferenceTableFactory {

	private RecordReaderFactory recordReaderFactory;
	private RecordDeleterFactory recordDeleterFactory;
	private RecordCreatorFactory recordCreatorFactory;
	private RecordUpdaterFactory recordUpdaterFactory;

	public ReferenceTableFactoryImp(RecordCreatorFactory recordCreatorFactory,
			RecordReaderFactory recordReaderFactory, RecordUpdaterFactory recordUpdaterFactory,
			RecordDeleterFactory recordDeleterFactory) {
		this.recordReaderFactory = recordReaderFactory;
		this.recordDeleterFactory = recordDeleterFactory;
		this.recordCreatorFactory = recordCreatorFactory;
		this.recordUpdaterFactory = recordUpdaterFactory;
	}

	@Override
	public ReferenceTable factor(String tableName) {
		if ("organisationAddress".equals(tableName)) {
			RecordCreator recordCreator = recordCreatorFactory.factor();
			RecordDeleter factoredDeleter = recordDeleterFactory.factor();
			return new OrganisationAddressTable(recordCreator, recordReaderFactory,
					recordUpdaterFactory, factoredDeleter);
		}
		throw NotImplementedException
				.withMessage("Reference table not implemented for " + tableName);

	}

	public RecordReaderFactory getRecordReaderFactory() {
		// needed for test
		return recordReaderFactory;
	}

	public RecordDeleterFactory getRecordDeleterFactory() {
		// needed for test
		return recordDeleterFactory;
	}

	public RecordCreatorFactory getRecordCreatorFactory() {
		// needed for test
		return recordCreatorFactory;
	}

	public RecordUpdaterFactory getRecordUpdaterFactory() {
		// needed for test
		return recordUpdaterFactory;
	}

}
