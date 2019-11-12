package se.uu.ub.cora.diva.mixedstorage.db;

import se.uu.ub.cora.sqldatabase.RecordUpdater;
import se.uu.ub.cora.sqldatabase.RecordUpdaterFactory;

public class RecordUpdaterFactorySpy implements RecordUpdaterFactory {

	public RecordUpdaterSpy factoredUpdater;
	public boolean factorWasCalled = false;

	@Override
	public RecordUpdater factor() {
		factorWasCalled = true;
		factoredUpdater = new RecordUpdaterSpy();
		return factoredUpdater;
	}

}
