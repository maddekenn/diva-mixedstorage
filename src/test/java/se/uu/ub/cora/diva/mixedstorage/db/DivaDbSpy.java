package se.uu.ub.cora.diva.mixedstorage.db;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;

public class DivaDbSpy implements DivaDb {

	public String type;
	public String id;
	public DataGroup dataGroup;

	@Override
	public DataGroup read(String type, String id) {
		this.type = type;
		this.id = id;
		dataGroup = new DataGroupSpy("DataGroupFromSpy");
		return dataGroup;
	}

}
