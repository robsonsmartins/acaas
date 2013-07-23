package com.robsonmartins.acaas.stt;
import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class STTResource extends ServerResource {
	
	private static final DefaultHttpClient fetcher = new DefaultHttpClient();
	private static final SpeechToText stt = new GoogleSpeechToText(fetcher);
	
	public boolean allowPost() {
		return true;
	}
	
	@Post
	public Representation postSTT(Representation param) throws Exception {
		Representation result = null;
		
		byte[] speech = IOUtils.toByteArray(param.getStream());
		String lang   = getQueryValue("lang");
		String format = getQueryValue("format");
		
		String text = stt.convert(speech, format, lang);
		result = new StringRepresentation(text, MediaType.TEXT_PLAIN);

		return result;
	}
}
