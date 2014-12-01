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

/**
 * Engine de transformacao de Texto para Fala.
 * <br/><br/>
 * Converte Texto para Fala atraves de:<br/>
 * <ul>
 * <li>Servico TTS do <a target="_blank" href="http://translate.google.com">Tradutor Google</a></li>
 * </ul>
 * <br/>
 * <b>Idiomas oferecidos</b>:<br/>
 *   <ul><li>Portugues Brasileiro (pt-BR)</li>
 *   <li><a target="_blank" href="http://translate.google.com.br/about/intl/pt-BR_ALL">Outros</a></li>
 *   </ul><br/>  
 * <b>Formatos de midia oferecidos</b>:<br/>
 *   <ul>
 *   	<li>Audio: WAV/PCM (padrao) ou FLAC</li>
 *   </ul><br/>  
 *   
 * @author Robson Martins (robson@robsonmartins.com) 
 */
public class GoogleTextToSpeech implements TextToSpeech {

	/** URL do TTS do Google. */
	private static final String GOOGLE_TTS_URL = "http://translate.google.com/translate_tts";
	/** Parametro CharCode do TTS do Google. */
	private static final String GOOGLE_TTS_CHARCODE_PARAM = "ie";
	/** Parametro Lang do TTS do Google. */
	private static final String GOOGLE_TTS_LANG_PARAM = "tl";
	/** Parametro Query do TTS do Google. */
	private static final String GOOGLE_TTS_QUERY_PARAM = "q";
	/** Parametro TextLen do TTS do Google. */
	private static final String GOOGLE_TTS_TEXTLEN_PARAM = "textlen";
	
	/** String de Idioma pt-BR (default). */
	private static final String GOOGLE_TTS_LANG_PT_BR = "pt-BR";
	
	/** Numero maximo de palavras a submeter ao TTS do Google. */
	private static final int GOOGLE_TTS_MAX_TEXT_WORDS = 50;
	/** Numero maximo de tentativas de conexao ao TTS do Google. */
	private static final int GOOGLE_TTS_MAX_TRY_CONN = 3;

	/** Cliente HTTP. */
	private DefaultHttpClient fetcher = null;

	/**
	 * Construtor.
	 * @param fetcher Cliente HTTP a ser usado.
	 */
	public GoogleTextToSpeech(DefaultHttpClient fetcher) {
		this.fetcher = fetcher;
	}
	
	@Override
	public byte[] getMedia(String text, String lang, String format) throws Exception {
		byte[] tokenData = null;
		AudioBuffer result = null;
		ByteArrayOutputStream pcmBuffer = new ByteArrayOutputStream();

		List<String> queries = tokenizeText(text); 
		for (String query : queries) {
			for (int nTry = 1; (nTry <= GOOGLE_TTS_MAX_TRY_CONN) && (tokenData == null); nTry++) {
				try {
					tokenData = convertOnePhrase(query, lang);
				} catch (Exception e) { 
					tokenData = null;
					if (nTry == GOOGLE_TTS_MAX_TRY_CONN) { throw e; }
				}
			}
			result = AudioProcessor.mpaToPcm(tokenData);
			pcmBuffer.write(result.getSamples());
		}
		
		result.setSamples(pcmBuffer.toByteArray());

		if (format == null) { format = ""; }
		format = format.toLowerCase();
		if (AudioFormats.FLAC.toStringList().contains(format)) {
			result = AudioProcessor.pcmToFlac(result);			
		} else { // WAV
			result = AudioProcessor.pcmToWav(result);
		}
		
		return result.getSamples();
	}

	@Override
	public byte[] getMedia(String text) throws Exception {
		return getMedia(text, null, null);
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
	
	/**
	 * Divide um texto em tokens (separados por espaco).
	 * @param text Texto a ser dividido.
	 * @return Lista de tokens.
	 */
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

	/**
	 * Converte um Texto para um objeto JSON.
	 * @param query Texto a ser convertido.
	 * @param lang Idioma do texto.
	 * @param format Formato desejado para o arquivo de midia.
	 * @return Objecto JSON que representa o Texto.
	 */
	private JSONObject generateJson(String query, String lang, String format) {
		Properties prop = new Properties();
		ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
		if (lang == null || lang.equals("")) { lang = GOOGLE_TTS_LANG_PT_BR; }
		try {
			if (query == null || query.equals("")) { 
				throw new NullPointerException("Text not found."); 
			}
			prop.setProperty("filename", query);
			prop.setProperty("lang", lang);
			prop.setProperty("format", (format != null) ? format : "");
			prop.store(outBuffer, null);
			String media = Base64.encode(outBuffer.toByteArray(), false);
			Map<String, String> map = new HashMap<String, String>();
			map.put("text", query);
			map.put("lang", lang);
			map.put("format", format);
			map.put("media", media);
			JSONObject result = new JSONObject(map);
			return result;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Converte uma frase para fala.
	 * @param text Texto a ser convertido.
	 * @param lang Idioma do texto.
	 * @return Dados binarios do audio.
	 * @throws Exception
	 */
	private byte[] convertOnePhrase(String text, String lang) throws Exception{
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
	
	/**
	 * Realiza uma requisicao HTTP GET.
	 * @param uri Endereco HTTP.
	 * @return Resposta HTTP.
	 * @throws Exception
	 */
	private HttpResponse getContent(String uri) throws Exception {
		HttpGet request = new HttpGet(uri);
		return fetcher.execute(request);
	}
}
