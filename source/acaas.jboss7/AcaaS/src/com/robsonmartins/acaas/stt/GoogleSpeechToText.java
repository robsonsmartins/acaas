package com.robsonmartins.acaas.stt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.robsonmartins.acaas.util.AudioBuffer;
import com.robsonmartins.acaas.util.AudioFormats;
import com.robsonmartins.acaas.util.AudioProcessor;

/**
 * Engine de transformacao de Fala para Texto.
 * <br/><br/>
 * Converte Fala para Texto atraves de:<br/>
 * <ul>
 * <li>Google <a target="_blank" href="https://gist.github.com/alotaiba/1730160">Speech To Text API</a></li>
 * </ul>
 * <br/>
 * <b>Idiomas oferecidos</b>:<br/>
 *   <ul><li>Portugues Brasileiro (pt-BR)</li>
 *   <li>Outros</li>
 *   </ul><br/>  
 * <b>Formatos de midia suportados</b>:<br/>
 *   <ul>
 *   	<li>Audio: WAV/PCM (padrao), FLAC ou MPEG (layer 1,2,3)</li>
 *   </ul><br/>  
 * <b>Modos de retorno JSON</b>:<br/>
 *   <ul>
 *   	<li>0: Compacto - Retorna somente a palavra mais provavel</li>
 *   	<li>1: Padrao - Igual ao Compacto</li>
 *   	<li>2: Detalhado - Retorna todas palavras provaveis</li>
 *   </ul><br/>
 *   
 * @author Robson Martins (robson@robsonmartins.com) 
 */
public class GoogleSpeechToText implements SpeechToText {
	
	/** URL do STT do Google. */
	private static final String GOOGLE_SPEECH_URL = "https://www.google.com/speech-api/v1/recognize";
	/** Parametro Lang do STT do Google. */
	private static final String GOOGLE_SPEECH_LANG_PARAM = "lang";
	/** Parametro Rate do STT do Google. */
	private static final String GOOGLE_SPEECH_RATE_PARAM = "rate";
	/** Parametros extras do STT do Google. */
	private static final String GOOGLE_SPEECH_EXTRA_PARAMS = "xjerr=1&client=chromium";
	/** Content-Type do STT do Google. */
	private static final String GOOGLE_SPEECH_CONTENT_TYPE = "audio/x-flac";
	
	/** Campo Hypoteses do resultado do STT do Google. */
	private static final String GOOGLE_SPEECH_RESULT_HYPOTHESES = "hypotheses";
	/** Campo Utterance do resultado do STT do Google. */
	private static final String GOOGLE_SPEECH_RESULT_UTTERANCE = "utterance";
	/** Campo Confidence do resultado do STT do Google. */
	private static final String GOOGLE_SPEECH_RESULT_CONFIDENCE = "confidence";

	/** String de Idioma pt-BR (default). */
	private static final String GOOGLE_SPEECH_LANG_PT_BR = "pt-BR";

	/** Modo de retorno JSON: modo 0. */
	private static final int GOOGLE_SPEECH_MODE_0 = 0;
	/** Modo de retorno JSON: modo 1 (default). */
	private static final int GOOGLE_SPEECH_MODE_1 = 1;
	/** Modo de retorno JSON: modo 2. */
	private static final int GOOGLE_SPEECH_MODE_2 = 2;
	
	/** Cliente HTTP. */
	private DefaultHttpClient fetcher = null;

	/** Numero maximo de tentativas de requisicao HTTP. */
	private static final int GOOGLE_SPEECH_MAX_HTTP_REQ_TRY = 3;
	
	/**
	 * Construtor.
	 * @param fetcher Cliente HTTP a ser usado.
	 */
	public GoogleSpeechToText(DefaultHttpClient fetcher) {
		this.fetcher = fetcher;
	}

	@Override
	public JSONObject convert(byte[] speech, String lang, String format, String mode) throws Exception {
		AudioBuffer pcmBuffer = null;
		List<List<String>> text = null;
		int nMode = GOOGLE_SPEECH_MODE_1;
		try {
			if (mode == null || mode.equals("")) { throw new NullPointerException(); }
			nMode = Integer.valueOf(mode);
		} catch (Exception e) { }
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
		AudioBuffer[] tokens = AudioProcessor.tokenize(pcmBuffer,200,-20.0);
		text = speechToText(tokens, lang);
		return textToJSON(text, lang, nMode);
	}

	@Override
	public JSONObject convert(byte[] speech, String lang, String format) throws Exception {
		return convert(speech, lang, format, null);
	}

	@Override
	public JSONObject convert(byte[] speech, String mode) throws Exception {
		return convert(speech, null, null, mode);
	}

	@Override
	public JSONObject convert(byte[] speech) throws Exception {
		return convert(speech, null, null, null);
	}
	
	/**
	 * Converte um Texto para um objeto JSON. 
	 * @param text Texto.
	 * @param lang Idioma.
	 * @param mode Modo de retorno JSON: 0 = Compacto, 1 = Padrao, 2 = Detalhado. 
	 * @return Objecto JSON que representa o Texto.
	 * @throws Exception
	 */
	private JSONObject textToJSON(List<List<String>> text,
			String lang, int mode) throws Exception {
		
		JSONArray possibilidades = null;
		JSONObject result = new JSONObject();
		JSONArray palavras = new JSONArray();
		
		for (List<String> item : text) {
			possibilidades = new JSONArray();
			switch (mode) {
				case GOOGLE_SPEECH_MODE_2:
					// modo detalhado: retorna todas Palavras Possiveis.
					for (String palavra : item) {
						possibilidades.put(palavra);
					}
					break;
				case GOOGLE_SPEECH_MODE_0:
				case GOOGLE_SPEECH_MODE_1:
				default:
					// modo padrao: retorna somente Palavra mais Provavel.
					if (item.size() != 0) {
						palavras.put(item.get(0));
					}
					break;
			}
			if (mode == GOOGLE_SPEECH_MODE_2) {
				JSONObject possJson = new JSONObject();
				possJson.put("possibilidades", possibilidades);
				palavras.put(possJson);
			}
		}
		result.put("items", palavras);
		return result;
	}
	
	/**
	 * Converte uma expressao em fala para Texto. 
	 * @param expressao Fala a ser convertida (tokens).
	 * @param lang Idioma da fala.
	 * @return Texto.
	 * @throws Exception
	 */
	private List<List<String>> speechToText(AudioBuffer[] expressao, String lang) throws Exception {
		List<List<String>> result = new ArrayList<List<String>>();
		if (expressao == null) { throw new NullPointerException("Speech not found."); }
		List<String> possibilidades = null;
		AudioBuffer flacBuffer = null;
		for (AudioBuffer token : expressao) {
			flacBuffer = AudioProcessor.pcmToFlac(token); 
			for (int t = 0; t < GOOGLE_SPEECH_MAX_HTTP_REQ_TRY && possibilidades == null; t++) {
				try {
					possibilidades = convertOnePhrase(flacBuffer.getSamples(),
							flacBuffer.getSampleRate(), lang);
				} catch (Exception e) { 
					possibilidades = null;
					if (t == GOOGLE_SPEECH_MAX_HTTP_REQ_TRY - 1) { throw e; }
				}
			}
			result.add(possibilidades);
		}
		return result;
	}
	
	/**
	 * Converte uma frase para texto.
	 * @param speech Fala a ser convertida.
	 * @param rate Taxa de amostragem, em samples/second.
	 * @param lang Idioma da fala.
	 * @return Possiveis palavras.
	 * @throws Exception
	 */
	private List<String> convertOnePhrase(byte[] speech, int rate, String lang) throws Exception{
		
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
			EntityUtils.consumeQuietly(response.getEntity());
			throw new IOException("HTTP Error: " + response.getStatusLine().getStatusCode());
		}
		String strJson = IOUtils.toString(response.getEntity().getContent(),"utf-8");
		JSONObject object = new JSONObject(strJson);
		JSONArray hypothesesArray =
			object.getJSONArray(GOOGLE_SPEECH_RESULT_HYPOTHESES);
		Map<Double, String> hypotheses = new TreeMap<Double, String>();
		for (int i = 0; i < hypothesesArray.length(); i++) {
			JSONObject hypothese = hypothesesArray.getJSONObject(i);
			String utterance =
				hypothese.getString(GOOGLE_SPEECH_RESULT_UTTERANCE);
			double confidence =
				hypothese.getDouble(GOOGLE_SPEECH_RESULT_CONFIDENCE);
			hypotheses.put(confidence, utterance);
		}
		
		List<String> result = new ArrayList<String>();
		Double[] keys = hypotheses.keySet().toArray(new Double[0]);
		for (int i = keys.length - 1; i >= 0; i--) {
			result.add(hypotheses.get(keys[i]));
		}
		return result;
	}
	
	/**
	 * Realiza uma requisicao HTTP POST.
	 * @param uri Endereco HTTP.
	 * @param data Dados para o corpo da requisicao.
	 * @param contentType Tipo de conteudo dos dados.
	 * @return Resposta HTTP.
	 * @throws Exception
	 */
	private HttpResponse postContent(String uri, byte[] data, String contentType) throws Exception {
		HttpPost req = new HttpPost(uri);
		req.setHeader("Content-Type", contentType);
		req.setHeader("Accept-Charset", "utf-8");
		ByteArrayEntity entity = new ByteArrayEntity(data);
		req.setEntity(entity);
		return fetcher.execute(req);
	}
	

}
