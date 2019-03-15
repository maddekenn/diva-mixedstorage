package se.uu.ub.cora.diva.mixedstorage.fedora;

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpMultiPartUploader;

public class HttpHandlerFactorySpy implements HttpHandlerFactory {
	public List<String> urls = new ArrayList<>();
	public List<HttpHandlerSpy> factoredHttpHandlers = new ArrayList<>();
	public String responseText = "";
	public int responseCode = 200;

	@Override
	public HttpHandler factor(String url) {
		urls.add(url);
		HttpHandlerSpy httpHandlerSpy = new HttpHandlerSpy();
		factoredHttpHandlers.add(httpHandlerSpy);
		httpHandlerSpy.responseText = responseText;
		httpHandlerSpy.responseCode = responseCode;
		return httpHandlerSpy;
	}

	@Override
	public HttpMultiPartUploader factorHttpMultiPartUploader(String url) {
		// TODO Auto-generated method stub
		return null;
	}

}
