package se.uu.ub.cora.diva.mixedstorage.db;

import java.util.ArrayList;
import java.util.List;

public class DivaDbToCoraConverterFactorySpy implements DivaDbToCoraConverterFactory {

	public List<DivaDbToCoraConverter> factoredConverters = new ArrayList<>();
	public List<String> factoredTypes = new ArrayList<>();

	@Override
	public DivaDbToCoraConverter factor(String type) {
		factoredTypes.add(type);
		DivaDbToCoraConverter converter = new DivaDbToCoraConverterSpy();
		factoredConverters.add(converter);
		return converter;
	}

}
