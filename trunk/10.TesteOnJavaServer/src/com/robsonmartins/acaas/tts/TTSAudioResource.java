package com.robsonmartins.acaas.tts;

import java.io.ByteArrayInputStream;
import java.util.Properties;

import org.apache.http.impl.client.DefaultHttpClient;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.util.Base64;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class TTSAudioResource extends ServerResource {

	private static final DefaultHttpClient fetcher = new DefaultHttpClient();
	private static final TextToSpeech tts = new GoogleTextToSpeech(fetcher);
	
	@Get
	public Representation getAudio() {
		String audio = getAttribute("audio");
		try {
			ByteArrayInputStream is = 
				new ByteArrayInputStream(Base64.decode(audio));
			Properties prop = new Properties();
			prop.load(is);
			String text = prop.getProperty("text");
			String lang = prop.getProperty("lang");
			String format = prop.getProperty("format");
			return textToSpeech(text, lang, format);
		} catch (Exception e) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
	}
	
	private Representation textToSpeech(String text, String lang, String format) throws Exception {
		Representation result = null;
		byte[] data = null;
		MediaType mediaType = MediaType.AUDIO_WAV;
		try {
			if (text == null || text == "") {
				throw new NullPointerException("Attribute \"text\" is required.");
			}
			if (format == null) { format = ""; }
			format = format.toUpperCase();	
			if (format.equals("FLAC")) {
				mediaType = MediaType.register("audio/x-flac", "FLAC Audio");
			} else { /* WAV */
				mediaType = MediaType.AUDIO_WAV;
				format = "WAV";
			}
			data = tts.getAudio(text, lang, format);
			if (data == null) {
				throw new NullPointerException(text + " not found.");
			}
		} catch (Exception e) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
		result = new ByteArrayRepresentation(data);
		result.setMediaType(mediaType);
		return result;
	}
	
	
}
