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
import java.util.ServiceLoader;

import se.uu.ub.cora.gatekeeper.user.GuestUserStorageProvider;
import se.uu.ub.cora.gatekeeper.user.UserStorage;
import se.uu.ub.cora.gatekeeper.user.UserStorageProvider;

public class DivaDbUserStorageProvider implements UserStorageProvider {

	private DivaMixedUserStorage userStorage;
	private Map<String, String> initInfo;
	private Iterable<GuestUserStorageProvider> guestUserStorageProviders;
	GuestUserStorageStarter guestUserStorageStarter = new GuestUserStorageStarterImp();
	// private ServiceLoader<GuestUserStorageProvider> guestUserStorageProviderImplementations;

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
		guestUserStorageProviders = ServiceLoader.load(GuestUserStorageProvider.class);
		guestUserStorageStarter.startUsingInitInfoAndGuestUserStorageProviders(initInfo,
				guestUserStorageProviders);

		// leta efter en guestUserProvider
		// UserStorage userStorageForGuest = new UserStorageImp(initInfo);
		userStorage = DivaMixedUserStorage.usingGuestUserStorageRecordReaderAndUserConverter(null,
				null, null);

	}

	public GuestUserStorageStarter getUserStorageStarter() {
		return guestUserStorageStarter;
	}

	public void setGuestUserStorageStarter(GuestUserStorageStarter guestUserStorageStarter) {
		this.guestUserStorageStarter = guestUserStorageStarter;

	}

	// private void getGuestUserStorageUsingModuleStarter() {
	// Iterable<GuestUserStorageProvider> guestUserStorageImplementations = ServiceLoader
	// .load(GuestUserStorageProvider.class);
	// guestUserStorageMuduleStarter.start
	// }

	// private void collectUserStorageImplementations() {
	// guestUserStorageProviderImplementations = ServiceLoader.load(GuestUserStorageProvider.class);
	// }

	// private <T extends SelectOrder> T getImplementationBasedOnPreferenceLevelThrowErrorIfNone(
	// Iterable<T> implementations, String interfaceClassName) {
	// T implementation = findAndLogPreferedImplementation(implementations, interfaceClassName);
	// throwErrorIfNoImplementationFound(interfaceClassName, implementation);
	// // log.logInfoUsingMessage("Using " + implementation.getClass().getName() + " as "
	// // + interfaceClassName + " implementation.");
	// return implementation;
	// }

	// private <T extends SelectOrder> T findAndLogPreferedImplementation(Iterable<T>
	// implementations,
	// String interfaceClassName) {
	// T implementation = null;
	// int preferenceLevel = -99999;
	// for (T currentImplementation : implementations) {
	// if (preferenceLevel < currentImplementation.getOrderToSelectImplementionsBy()) {
	// preferenceLevel = currentImplementation.getOrderToSelectImplementionsBy();
	// implementation = currentImplementation;
	// }
	// log.logInfoUsingMessage(FOUND + currentImplementation.getClass().getName() + " as "
	// + interfaceClassName + " implementation with select order "
	// + currentImplementation.getOrderToSelectImplementionsBy() + ".");
	// }
	// return implementation;
	// }

	// private <T extends SelectOrder> void throwErrorIfNoImplementationFound(
	// String interfaceClassName, T implementation) {
	// if (null == implementation) {
	// String errorMessage = "No implementations found for " + interfaceClassName;
	// // log.logFatalUsingMessage(errorMessage);
	// throw DbException.withMessage(errorMessage);
	// }
	// }

}
