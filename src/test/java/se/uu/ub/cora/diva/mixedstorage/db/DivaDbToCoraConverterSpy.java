package se.uu.ub.cora.diva.mixedstorage.db;

import java.util.Map;

import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.diva.mixedstorage.DataGroupSpy;

public class DivaDbToCoraConverterSpy implements DivaDbToCoraConverter {
	public Map<String, Object> mapToConvert;
	public DataGroup convertedDbDataGroup;

	@Override
	public DataGroup fromMap(Map<String, Object> map) {
		mapToConvert = map;
		convertedDbDataGroup = new DataGroupSpy("from Db converter");
		return convertedDbDataGroup;
	}
}
