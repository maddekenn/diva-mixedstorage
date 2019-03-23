package se.uu.ub.cora.diva.mixedstorage.db;

import java.util.Map;

public class PreparedStatementInfo {

	public final String tableName;
	public final Map<String, Object> values;
	public final Map<String, Object> conditions;

	public static PreparedStatementInfo withTableNameValuesAndConditions(String tableName,
			Map<String, Object> values, Map<String, Object> conditions) {
		return new PreparedStatementInfo(tableName, values, conditions);
	}

	private PreparedStatementInfo(String tableName, Map<String, Object> values,
			Map<String, Object> conditions) {
		this.tableName = tableName;
		this.values = values;
		this.conditions = conditions;
	}

}
