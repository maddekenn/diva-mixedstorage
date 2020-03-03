package se.uu.ub.cora.diva.mixedstorage.db;

import java.util.ArrayList;
import java.util.List;

public class DivaDbFactorySpy implements DivaDbFactory {
	public boolean factorWasCalled = false;
	public DivaDbSpy factored;
	public String type;
	public List<DivaDbSpy> factoredList = new ArrayList<>();

	@Override
	public DivaDbReader factor(String type) {
		factorWasCalled = true;
		this.type = type;
		factored = new DivaDbSpy();
		factoredList.add(factored);
		return factored;
	}

}
