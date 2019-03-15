package se.uu.ub.cora.diva.mixedstorage.db;

import se.uu.ub.cora.bookkeeper.data.DataGroup;

public interface DivaDbToCora {

	DataGroup convertOneRowData(String type, String id);

}
