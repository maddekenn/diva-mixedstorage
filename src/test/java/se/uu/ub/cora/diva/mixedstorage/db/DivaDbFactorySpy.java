package se.uu.ub.cora.diva.mixedstorage.db;

public class DivaDbFactorySpy implements DivaDbFactory {
	public boolean factorWasCalled = false;
	public DivaDbSpy factored;
	public String type;

	@Override
	public DivaDb factor(String type) {
		factorWasCalled = true;
		this.type = type;
		factored = new DivaDbSpy();
		return factored;
	}

}
