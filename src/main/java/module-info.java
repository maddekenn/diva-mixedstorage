module se.uu.ub.cora.diva.mixedstorage {
	requires transitive se.uu.ub.cora.sqldatabase;
	requires transitive se.uu.ub.cora.spider;
	requires transitive se.uu.ub.cora.httphandler;
	requires transitive java.xml;

	exports se.uu.ub.cora.diva.mixedstorage.db;
	exports se.uu.ub.cora.diva.mixedstorage.fedora;

	opens person;
}