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
package se.uu.ub.cora.diva.mixedstorage.db.organisation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OrganisationType {
	private static final Map<Integer, String> immutableOrganisationType;
	static {
		Map<Integer, String> mutableMap = new HashMap<>();
		mutableMap.put(49, "root");
		mutableMap.put(50, "university");
		mutableMap.put(51, "school");
		mutableMap.put(52, "faculty");
		mutableMap.put(53, "centre");
		mutableMap.put(54, "department");
		mutableMap.put(55, "unit");
		mutableMap.put(56, "section");
		mutableMap.put(57, "domain");
		mutableMap.put(58, "researchGroup");
		mutableMap.put(59, "researchProgram");
		mutableMap.put(60, "researchProject");
		mutableMap.put(61, "division");
		mutableMap.put(62, "museum");
		mutableMap.put(63, "researchInstitute");
		mutableMap.put(64, "authority");
		mutableMap.put(65, "company");
		mutableMap.put(66, "other");
		immutableOrganisationType = Collections.unmodifiableMap(mutableMap);
	}

	private OrganisationType() {
		// not called
		throw new UnsupportedOperationException();
	}

	public static String getTypeCodeForIntValue(int intValue) {
		return immutableOrganisationType.get(intValue);
	}
}
