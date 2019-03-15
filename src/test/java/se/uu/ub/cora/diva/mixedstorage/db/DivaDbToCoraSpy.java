package se.uu.ub.cora.diva.mixedstorage.db;

import se.uu.ub.cora.bookkeeper.data.DataGroup;

public class DivaDbToCoraSpy implements DivaDbToCora {

	public String type;
	public String id;
	public DataGroup dataGroup;

	@Override
	public DataGroup convertOneRowData(String type, String id) {
		this.type = type;
		this.id = id;
		dataGroup = DataGroup.withNameInData("DataGroupFromSpy");
		return dataGroup;
	}

}
