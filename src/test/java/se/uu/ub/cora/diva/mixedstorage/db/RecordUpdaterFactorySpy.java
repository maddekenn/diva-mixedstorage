package se.uu.ub.cora.diva.mixedstorage.db;

import se.uu.ub.cora.sqldatabase.RecordUpdater;
import se.uu.ub.cora.sqldatabase.RecordUpdaterFactory;

public class RecordUpdaterFactorySpy implements RecordUpdaterFactory {

	public RecordUpdater factored;

	@Override
	public RecordUpdater factor() {
		factored = new RecordUpdaterSpy();
		return factored;
	}

}
