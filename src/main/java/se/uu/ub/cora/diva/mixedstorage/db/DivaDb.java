package se.uu.ub.cora.diva.mixedstorage.db;

import se.uu.ub.cora.data.DataGroup;

public interface DivaDb {

	DataGroup read(String type, String id);

}
