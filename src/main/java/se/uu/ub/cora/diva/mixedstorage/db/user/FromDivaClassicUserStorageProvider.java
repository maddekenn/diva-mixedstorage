/*
 * Copyright 2020 Uppsala University Library
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
package se.uu.ub.cora.diva.mixedstorage.db.user;

import java.util.Map;

import se.uu.ub.cora.basicstorage.UserStorageImp;
import se.uu.ub.cora.gatekeeper.user.UserStorage;
import se.uu.ub.cora.gatekeeper.user.UserStorageProvider;

public class FromDivaClassicUserStorageProvider implements UserStorageProvider {

	private DivaMixedUserStorage userStorage;
	private Map<String, String> initInfo;

	@Override
	public int getOrderToSelectImplementionsBy() {
		return 10;
	}

	@Override
	public UserStorage getUserStorage() {
		return userStorage;
	}

	@Override
	public void startUsingInitInfo(Map<String, String> initInfo) {
		this.initInfo = initInfo;

		// leta efter en guestUserProvider
		UserStorage userStorageForGuest = new UserStorageImp(initInfo);
		userStorage = DivaMixedUserStorage
				.usingGuestUserStorageRecordReaderAndUserConverter(userStorageForGuest, null, null);

	}

}
