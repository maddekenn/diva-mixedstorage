package se.uu.ub.cora.diva.mixedstorage.fedora;

import se.uu.ub.cora.data.DataGroup;

public class DivaFedoraToCoraConverterSpy implements DivaFedoraToCoraConverter {

	public String xml;
	public DataGroup convertedDataGroup;

	@Override
	public DataGroup fromXML(String xml) {
		this.xml = xml;
		convertedDataGroup = DataGroup.withNameInData("Converted xml");
		return convertedDataGroup;
	}

}
