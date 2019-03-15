package se.uu.ub.cora.diva.mixedstorage.db;

public interface DivaDbToCoraConverterFactory {

	DivaDbToCoraConverter factor(String type);

}
