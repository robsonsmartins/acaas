package com.robsonmartins.acaas.tts;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class TTSResource extends ServerResource {
	
	private static final DefaultHttpClient fetcher = new DefaultHttpClient();
	private static final TextToSpeech tts = new GoogleTextToSpeech(fetcher);
	
	public boolean allowPost() {
		return true;
	}
	
	@Post("text")
	public Representation postTTS(Representation param) throws Exception {
		String lang = getQueryValue("lang");
		String text = param.getText();
		String format = getQueryValue("format");
		return textToSpeech(text, lang, format);
	}
	
	@Get
	public Representation getTTS() throws Exception {
		String text = getQueryValue("text");
		String lang = getQueryValue("lang");
		String format = getQueryValue("format");
		return textToSpeech(text, lang, format);
	}
	
	private Representation textToSpeech(String text, String lang, String format) {
		Representation result = null;
		JSONObject data = null;
		try {
			if (text == null || text == "") {
				throw new NullPointerException("Attribute \"text\" is required.");
			}
			if (format == null) { format = ""; }
			format = format.toUpperCase();	
			if (format.equals("FLAC")) {
				format = "audio/x-flac";
			} else { /* WAV */
				format = MediaType.AUDIO_WAV.getName();
			}
			data = tts.convert(text, lang, format);
		} catch (Exception e) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
		result = new JsonRepresentation(data);
		result.setMediaType(MediaType.APPLICATION_JSON);
		return result;
	}
}
