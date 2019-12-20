package se.uu.ub.cora.diva.mixedstorage.db;

public enum OrganisationColumns {
	NAME("organisation_name", "organisationName", "string"), CLOSED_DATE("closed_date",
			"closedDate", "date"), ORGANISATION_CODE("organisation_code", "organisationCode",
					"string"), ORGANISATION_NUMBER("orgnumber", "organisationNumber", "string");

	public final String dbName;
	public final String coraName;
	public final String type;

	OrganisationColumns(String dbName, String coraName, String type) {
		this.dbName = dbName;
		this.coraName = coraName;
		this.type = type;
	}

}
