package se.uu.ub.cora.diva.mixedstorage.db;

import se.uu.ub.cora.bookkeeper.data.DataGroup;

public interface CoraToDbConverter {

	PreparedStatementInfo convert(DataGroup dataGroup);

}
