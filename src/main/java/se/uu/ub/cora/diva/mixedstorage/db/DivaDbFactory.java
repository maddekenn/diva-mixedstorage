package se.uu.ub.cora.diva.mixedstorage.db;

public interface DivaDbFactory {

	DivaDbReader factor(String type);

}
