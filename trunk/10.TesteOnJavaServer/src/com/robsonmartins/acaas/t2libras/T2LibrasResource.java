package com.robsonmartins.acaas.t2libras;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class T2LibrasResource extends ServerResource {
	
	private static final DefaultHttpClient fetcher = new DefaultHttpClient();
	private static final TextToLibras t2libras = new AccBrasilTextToLibras(fetcher);
	
	public boolean allowPost() {
		return true;
	}
	
	@Post("text")
	public Representation postT2Libras(Representation param) throws Exception {
		String lang = getQueryValue("lang");
		String text = param.getText();
		return textToLibras(text, lang);
	}
	
	@Get
	public Representation getT2Libras() throws Exception {
		String text = getQueryValue("text");
		String lang = getQueryValue("lang");
		return textToLibras(text, lang);
	}
	
	private Representation textToLibras(String text, String lang) throws Exception {
		Representation result = null;
		if (text == null) {
			throw new NullPointerException("Attribute \"text\" is required.");
		}
		JSONObject data = t2libras.convert(text, lang);
		result = new JsonRepresentation(data);
		result.setMediaType(MediaType.APPLICATION_JSON);
		return result;
	}
}
