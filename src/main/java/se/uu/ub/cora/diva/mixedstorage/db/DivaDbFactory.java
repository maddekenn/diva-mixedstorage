package se.uu.ub.cora.diva.mixedstorage.db;

import se.uu.ub.cora.diva.mixedstorage.db.organisation.MultipleRowDbToDataReader;

public interface DivaDbFactory {

	DivaDbReader factor(String type);

	MultipleRowDbToDataReader factorMultipleReader(String type);

}
