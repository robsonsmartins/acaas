package com.robsonmartins.acaas.stt;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.robsonmartins.acaas.util.AudioBuffer;
import com.robsonmartins.acaas.util.AudioFormats;
import com.robsonmartins.acaas.util.AudioProcessor;

public class GoogleSpeechToText implements SpeechToText {
	
	private static final String GOOGLE_SPEECH_URL = "https://www.google.com/speech-api/v1/recognize";
	private static final String GOOGLE_SPEECH_LANG_PARAM = "lang";
	private static final String GOOGLE_SPEECH_EXTRA_PARAMS = "xjerr=1&client=chromium";
	private static final String GOOGLE_SPEECH_LANG_PT_BR = "pt-BR";
	private static final String GOOGLE_SPEECH_RATE_PARAM = "rate";
	private static final String GOOGLE_SPEECH_CONTENT_TYPE = "audio/x-flac";

	private static final String GOOGLE_SPEECH_RESULT_HYPOTHESES = "hypotheses";
	private static final String GOOGLE_SPEECH_RESULT_UTTERANCE = "utterance";
	private static final String GOOGLE_SPEECH_RESULT_CONFIDENCE = "confidence";
	private static final String GOOGLE_SPEECH_RESULT_UNKNOWN = "<desconhecido>";
	
	private DefaultHttpClient fetcher = null;

	public GoogleSpeechToText(DefaultHttpClient fetcher) {
		this.fetcher = fetcher;
	}

	@Override
	public String convert(byte[] speech, String format, String language) throws Exception {
		AudioBuffer pcmBuffer = null;
		AudioBuffer flacBuffer = null;
		if (format == null) { format = ""; }
		format = format.toUpperCase();
		if (AudioFormats.MPEG.toStringList().contains(format)) {
			pcmBuffer = AudioProcessor.mpaToPcm(speech);
		} else if (AudioFormats.FLAC.toStringList().contains(format)) {
			pcmBuffer = AudioProcessor.flacToPcm(speech);
		} else { /* WAV */
			pcmBuffer = AudioProcessor.wavToPcm(speech);
		}
		pcmBuffer = AudioProcessor.normalize(pcmBuffer);
		flacBuffer = AudioProcessor.pcmToFlac(pcmBuffer); 
		return convertOnePhrase(flacBuffer.getSamples(),
				flacBuffer.getSampleRate(), language);
	}

	@Override
	public String convert(byte[] speech) throws Exception {
		return convert(speech, null, null);
	}
	
	private String convertOnePhrase(byte[] speech, int rate, String lang) throws IOException{
		
		if (lang == null || lang.equals("")) { lang = GOOGLE_SPEECH_LANG_PT_BR; }
		
		StringBuilder urlStr = new StringBuilder();
		urlStr.append(GOOGLE_SPEECH_URL)
			.append("?")
			.append(GOOGLE_SPEECH_EXTRA_PARAMS)
			.append("&")
			.append(GOOGLE_SPEECH_LANG_PARAM).append("=").append(lang);
		StringBuilder contentType = new StringBuilder();
		contentType.append(GOOGLE_SPEECH_CONTENT_TYPE).append("; ");
		contentType.append(GOOGLE_SPEECH_RATE_PARAM).append("=").append(rate);
		HttpResponse response =
			postContent(urlStr.toString(), speech, contentType.toString());
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			throw new IOException("HTTP Error: " + response.getStatusLine().getStatusCode());
		}
		JsonParser parser = new JsonParser();
		String strJson = IOUtils.toString(response.getEntity().getContent());
		JsonObject object = parser.parse(strJson).getAsJsonObject();
		JsonArray hypothesesArray =
			object.getAsJsonArray(GOOGLE_SPEECH_RESULT_HYPOTHESES);
		Map<Float, String> hypotheses = new TreeMap<Float, String>();
		for (JsonElement hypElement : hypothesesArray) {
			JsonObject hypothese = hypElement.getAsJsonObject();
			String utterance =
				hypothese.get(GOOGLE_SPEECH_RESULT_UTTERANCE).getAsString();
			float confidence =
				hypothese.get(GOOGLE_SPEECH_RESULT_CONFIDENCE).getAsFloat();
			hypotheses.put(confidence, utterance);
		}
		if (hypotheses.size() > 0) {
			Float key = hypotheses.keySet().toArray(new Float[0])[hypotheses.size() - 1];
			return hypotheses.get(key);
		} else {
			return GOOGLE_SPEECH_RESULT_UNKNOWN;
		}
	}
	
	private HttpResponse postContent(String uri, byte[] data, String contentType) throws IOException {
		HttpPost req = new HttpPost(uri);
		req.setHeader("Content-Type", contentType);
		ByteArrayEntity entity = new ByteArrayEntity(data);
		req.setEntity(entity);
		return fetcher.execute(req);
	}
	

}
