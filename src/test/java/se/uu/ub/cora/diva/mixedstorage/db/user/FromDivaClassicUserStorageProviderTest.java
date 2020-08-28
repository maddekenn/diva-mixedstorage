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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.basicstorage.UserStorageImp;
import se.uu.ub.cora.data.DataGroupFactory;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.diva.mixedstorage.DataGroupFactorySpy;
import se.uu.ub.cora.diva.mixedstorage.log.LoggerFactorySpy;
import se.uu.ub.cora.gatekeeper.user.UserStorageProvider;
import se.uu.ub.cora.logger.LoggerProvider;

public class FromDivaClassicUserStorageProviderTest {
	private String basePath = "/tmp/recordStorageOnDiskTemp/";
	private LoggerFactorySpy loggerFactorySpy;
	private DataGroupFactory dataGroupFactory;
	private UserStorageProvider userStorageProvider;
	private Map<String, String> initInfo;

	@BeforeMethod
	public void setUp() throws IOException {
		setUpFactoriesAndProviders();
		setUpInitInfo();
		// File dir = new File(basePath);
		// dir.mkdir();
		// deleteFiles(basePath);
		// TestDataAppTokenStorage.createRecordStorageInMemoryWithTestData(basePath);

		userStorageProvider = new FromDivaClassicUserStorageProvider();
	}

	private void setUpFactoriesAndProviders() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		dataGroupFactory = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactory);
	}

	private void setUpInitInfo() {
		initInfo = new HashMap<>();
		initInfo.put("storageOnDiskBasePath", basePath);
		initInfo.put("databaseLookupName", "java:/comp/env/jdbc/postgres");
	}

	private void deleteFiles(String path) throws IOException {
		Stream<Path> list;
		list = Files.list(Paths.get(path));

		list.forEach(p -> deleteFile(p));
		list.close();
	}

	private void deleteFile(Path path) {
		try {
			if (path.toFile().isDirectory()) {
				deleteFiles(path.toString());
			}
			Files.delete(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@AfterMethod
	public void removeTempFiles() throws IOException {
		if (Files.exists(Paths.get(basePath))) {
			deleteFiles(basePath);
			File dir = new File(basePath);
			dir.delete();
		}
	}

	@Test
	public void testPreferenceLevel() {
		userStorageProvider.startUsingInitInfo(null);
		assertEquals(userStorageProvider.getOrderToSelectImplementionsBy(), 10);
	}

	@Test
	public void testInit() throws Exception {
		userStorageProvider.startUsingInitInfo(initInfo);
		DivaMixedUserStorage userStorage = (DivaMixedUserStorage) userStorageProvider
				.getUserStorage();

		UserStorageImp userStorageForGuest = (UserStorageImp) userStorage.getUserStorageForGuest();

		assertSame(userStorageForGuest.getInitInfo(), initInfo);
		// assertEquals(userStorageForGuest.getClass(), UserStorageImp.class);
		//
		// DataReaderImp dataReader = (DataReaderImp) userStorage.getDataReaderForUsers();
		// assertEquals(dataReader.getClass(), DataReaderImp.class);
		//
		// SqlConnectionProvider sqlConnectionProvider = dataReader.getSqlConnectionProvider();
		// assertEquals(sqlConnectionProvider.getClass(), ContextConnectionProviderImp.class);
		//
		// ContextConnectionProviderImp contextConnectionProviderImp =
		// (ContextConnectionProviderImp) sqlConnectionProvider;
		//
		// assertEquals(contextConnectionProviderImp.getName(), initInfo.get("databaseLookupName"));
		// assertTrue(contextConnectionProviderImp.getContext() instanceof InitialContext);
	}
}
