package se.uu.ub.cora.diva.mixedstorage.db;

import java.util.Map;

import se.uu.ub.cora.bookkeeper.data.DataGroup;

public class DivaDbToCoraConverterSpy implements DivaDbToCoraConverter {
	public Map<String, String> mapToConvert;
	public DataGroup convertedDataGroup;
	public DataGroup convertedDbDataGroup;

	@Override
	public DataGroup fromMap(Map<String, String> map) {
		mapToConvert = map;
		convertedDbDataGroup = DataGroup.withNameInData("from Db converter");
		return convertedDbDataGroup;
	}
}
