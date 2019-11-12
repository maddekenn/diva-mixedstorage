package se.uu.ub.cora.diva.mixedstorage.db;

import java.util.Map;

import se.uu.ub.cora.data.DataGroup;

public class DivaDbToCoraConverterSpy implements DivaDbToCoraConverter {
	public Map<String, Object> mapToConvert;
	public DataGroup convertedDataGroup;
	public DataGroup convertedDbDataGroup;

	@Override
	public DataGroup fromMap(Map<String, Object> map) {
		mapToConvert = map;
		convertedDbDataGroup = DataGroup.withNameInData("from Db converter");
		return convertedDbDataGroup;
	}
}
