package se.uu.ub.cora.diva.mixedstorage.db;

public enum OrganisationColumns {
	NAME("organisation_name", "organisationName"), CLOSED_DATE("closed_date",
			"closedDate"), ORGANISATION_CODE("organisation_code",
					"organisationCode"), ORGANISATION_NUMBER("orgnumber", "organisationNumber");

	public final String dbName;
	public final String coraName;

	OrganisationColumns(String dbName, String coraName) {
		this.dbName = dbName;
		this.coraName = coraName;
	}

}
