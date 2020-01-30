/*
 * Copyright 2018, 2019 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.uu.ub.cora.diva.mixedstorage.db;

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.sqldatabase.RecordReader;
import se.uu.ub.cora.sqldatabase.RecordReaderFactory;

public class RecordReaderFactorySpy implements RecordReaderFactory {

	public boolean factorWasCalled;
	public RecordReaderSpy factored;
	public int noOfRecordsToReturn = 1;
	public int numOfPredecessorsToReturn = 0;
	public int numOfSuccessorsToReturn = 0;
	public int numOfParentsToReturn = 0;
	public List<RecordReaderSpy> factoredReaders = new ArrayList<>();

	@Override
	public RecordReader factor() {
		factorWasCalled = true;
		factored = new RecordReaderSpy();
		factoredReaders.add(factored);
		factored.numOfPredecessorsToReturn = numOfPredecessorsToReturn;
		factored.numOfSuccessorsToReturn = numOfSuccessorsToReturn;
		factored.numOfParentsToReturn = numOfParentsToReturn;
		factored.noOfRecordsToReturn = noOfRecordsToReturn;
		return factored;
	}

}
