package com.robsonmartins.acaas.tts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.engine.util.Base64;

import com.robsonmartins.acaas.util.AudioBuffer;
import com.robsonmartins.acaas.util.AudioFormats;
import com.robsonmartins.acaas.util.AudioProcessor;

public class GoogleTextToSpeech implements TextToSpeech {

	private static final String GOOGLE_TTS_URL = "http://translate.google.com/translate_tts";
	private static final String GOOGLE_TTS_CHARCODE_PARAM = "ie";
	private static final String GOOGLE_TTS_LANG_PARAM = "tl";
	private static final String GOOGLE_TTS_QUERY_PARAM = "q";
	private static final String GOOGLE_TTS_TEXTLEN_PARAM = "textlen";
	private static final String GOOGLE_TTS_LANG_PT_BR = "pt-BR";
	
	private static final int GOOGLE_TTS_MAX_TEXT_WORDS = 50;

	private DefaultHttpClient fetcher = null;

	public GoogleTextToSpeech(DefaultHttpClient fetcher) {
		this.fetcher = fetcher;
	}
	
	@Override
	public byte[] getAudio(String text, String language, String format) throws Exception {
		byte[] tokenData = null;
		AudioBuffer result = null;
		ByteArrayOutputStream pcmBuffer = new ByteArrayOutputStream();

		List<String> queries = tokenizeText(text); 
		for (String query : queries) {
			tokenData = convertOnePhrase(query, language);
			result = AudioProcessor.mpaToPcm(tokenData);
			pcmBuffer.write(result.getSamples());
		}
		
		result.setSamples(pcmBuffer.toByteArray());

		if (format == null) { format = ""; }
		format = format.toUpperCase();
		if (AudioFormats.FLAC.toStringList().contains(format)) {
			result = AudioProcessor.pcmToFlac(result);			
		} else { /* WAV */
			result = AudioProcessor.pcmToWav(result);
		}
		
		return result.getSamples();
	}

	@Override
	public byte[] getAudio(String text) throws Exception {
		return getAudio(text, null, null);
	}
	
	@Override
	public JSONObject convert(String text, String language, String format) throws Exception {
		JSONObject result = new JSONObject();
		JSONArray items = new JSONArray();
		JSONObject tokenData = null;

		List<String> queries = tokenizeText(text); 
		for (String query : queries) {
			tokenData = generateJson(query, language, format);
			if (tokenData != null) { items.put(tokenData); }
		}
		result.put("items", items);
		return result;
	}

	@Override
	public JSONObject convert(String text) throws Exception {
		return convert(text, null, null);
	}
	
	private List<String> tokenizeText(String text) {
		List<String> queries = new ArrayList<String>();
		String token = null;
		String tokens[] = text.split(" ");
		int tokenIdx = 0;
		while (tokenIdx < tokens.length) {
			token = "";
			while (tokenIdx < tokens.length &&
					token.length() < GOOGLE_TTS_MAX_TEXT_WORDS) {
				
				if (token.length() != 0) { token += " "; }
				token += tokens[tokenIdx];
				tokenIdx++;
			}
			if (token.length() != 0) { queries.add(token); }
		}
		return queries;
	}

	private JSONObject generateJson(String query, String language, String format) {
		Properties prop = new Properties();
		ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
		if (language == null || language.equals("")) { language = GOOGLE_TTS_LANG_PT_BR; }
		try {
			if (query == null || query.equals("")) { 
				throw new NullPointerException("Text not found."); 
			}
			prop.setProperty("text", query);
			prop.setProperty("lang", language);
			prop.setProperty("format", (format != null) ? format : "");
			prop.store(outBuffer, null);
			String audio = Base64.encode(outBuffer.toByteArray(), false);
			Map<String, String> map = new HashMap<String, String>();
			map.put("text", query);
			map.put("lang", language);
			map.put("format", format);
			map.put("audio", audio);
			JSONObject result = new JSONObject(map);
			return result;
		} catch (Exception e) {
			return null;
		}
	}
	
	private byte[] convertOnePhrase(String text, String lang) throws IOException{
		if (lang == null || lang.equals("")) { lang = GOOGLE_TTS_LANG_PT_BR; }
		StringBuilder urlStr = new StringBuilder();
		urlStr.append(GOOGLE_TTS_URL)
			.append("?")
			.append(GOOGLE_TTS_CHARCODE_PARAM).append("=").append("UTF-8")
			.append("&")
			.append(GOOGLE_TTS_LANG_PARAM).append("=").append(lang)
			.append("&")
			.append(GOOGLE_TTS_TEXTLEN_PARAM).append("=").append(text.length())
			.append("&")
			.append(GOOGLE_TTS_QUERY_PARAM).append("=").append(URLEncoder.encode(text,"UTF-8"));
		HttpResponse response = getContent(urlStr.toString());
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			throw new IOException("HTTP Error: " + response.getStatusLine().getStatusCode());
		}
		return IOUtils.toByteArray(response.getEntity().getContent());
	}
	
	private HttpResponse getContent(String uri) throws IOException {
		HttpGet request = new HttpGet(uri);
		return fetcher.execute(request);
	}
}
