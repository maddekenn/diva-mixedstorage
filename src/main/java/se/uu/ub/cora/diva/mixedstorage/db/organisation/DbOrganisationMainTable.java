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

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.db.DataToDbTranslater;
import se.uu.ub.cora.diva.mixedstorage.db.DbMainTable;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTable;
import se.uu.ub.cora.diva.mixedstorage.db.RelatedTableFactory;
import se.uu.ub.cora.sqldatabase.RecordUpdater;

public class DbOrganisationMainTable implements DbMainTable {

	private DataToDbTranslater dataToDbTranslater;
	private RecordUpdater recordUpdater;
	private RelatedTableFactory relatedTableFactory;

	public DbOrganisationMainTable(DataToDbTranslater dataTranslater, RecordUpdater recordUpdater,
			RelatedTableFactory relatedTableFactory) {
		this.dataToDbTranslater = dataTranslater;
		this.recordUpdater = recordUpdater;
		this.relatedTableFactory = relatedTableFactory;
	}

	@Override
	public void update(DataGroup dataGroup) {
		dataToDbTranslater.translate(dataGroup);
		recordUpdater.updateTableUsingNameAndColumnsWithValuesAndConditions("organisation",
				dataToDbTranslater.getValues(), dataToDbTranslater.getConditions());
		RelatedTable alternativeName = relatedTableFactory.factor("organisationAlternativeName");
		alternativeName.handleDbForDataGroup(dataGroup);

		RelatedTable parent = relatedTableFactory.factor("organisationParent");
		parent.handleDbForDataGroup(dataGroup);
		// TODO:uppdatera parent om ändrat -OBS repeateble

		// TODO: hantera transaktioner??
		// Faktorera OrganisationAlternativeNameRelatedTable
		// TODO: uppdatera preecessor om ändrat
		// TODO: uppdatera adress om det är ändrat
		// TODO:uppdatera organisation type om ändrat
	}

	public DataToDbTranslater getDataToDbTranslater() {
		// needed for test
		return dataToDbTranslater;
	}

	public RecordUpdater getRecordUpdater() {
		// needed for test
		return recordUpdater;
	}

	public RelatedTableFactory getRelatedTableFactory() {
		// needed for test
		return relatedTableFactory;
	}

}
