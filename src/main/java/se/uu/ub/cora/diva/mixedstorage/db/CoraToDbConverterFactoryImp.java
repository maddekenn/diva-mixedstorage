package se.uu.ub.cora.diva.mixedstorage.db;

import se.uu.ub.cora.diva.mixedstorage.NotImplementedException;

public class CoraToDbConverterFactoryImp implements CoraToDbConverterFactory {

	@Override
	public CoraToDbConverter factor(String type) {
		if ("divaOrganisation".equals(type)) {
			return new CoraToDbOrganisationConverter();
		}
		throw NotImplementedException.withMessage("No converter implemented for: " + type);
	}

}
