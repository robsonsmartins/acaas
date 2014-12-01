package com.robsonmartins.acaas.stt;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.header.Header;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.util.Series;

import com.robsonmartins.acaas.JsonResource;


/**
 * Servico REST para transformacao de Fala em Texto.
 * <p/>
 * <code>speech (POST)</code> - Arquivo de audio a ser convertido.<br/>
 * <code>lang</code> - Idioma do texto.<br/>
 * <code>format</code> - Formato do arquivo de audio.<br/>
 * <code>mode</code> - Modo de retorno JSON.<br/>
 *   
 * @author Robson Martins (robson@robsonmartins.com) 
 */
public class STTResource extends JsonResource {
	
	/** Engine de transformacao de Fala para Texto. */ 
	protected static SpeechToText stt = null;
	
	/** 
	 * Construtor.<br/>
	 * Usa implementacao {@link GoogleSpeechToText}.
	 */
	public STTResource() {
		super();
		// usa implementacao GoogleSpeechToText
		stt = new GoogleSpeechToText(fetcher);
	}

	/**
	 * Permite somente metodo POST e OPTIONS.
	 * @return False.
	 */
	@Override
	public Set<Method> getAllowedMethods() {
		Set<Method> methods = new TreeSet<Method>();
		methods.add(Method.OPTIONS);
		methods.add(Method.POST);
		return methods;
	}
	
	/**
	 * Trata requsicoes via metodo OPTIONS.
	 * @param entity Corpo da Requisicao.
	 */
	@Override
	public void doOptions(Representation entity) {
		try {
			super.doOptions(entity);
			@SuppressWarnings("unchecked")
		    Series<Header> responseHeaders = 
		    	(Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers"); 
		    if (responseHeaders == null) { 
		        responseHeaders = new Series<Header>(Header.class); 
		        getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders); 
		    } 
		    responseHeaders.add("Access-Control-Allow-Methods", "OPTIONS,POST");
		} catch (Exception e) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
	}
	
	/**
	 * Responde requsicoes via metodo POST.
	 * @param param Corpo da Requisicao (contem o parametro <code>speech</code>).
	 * @return Resposta (JSON).
	 */
	@Override
	@Post("text")
	public Representation responsePost(Representation param) {
		try {
			@SuppressWarnings("unchecked")
		    Series<Header> responseHeaders = 
		    	(Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers"); 
		    if (responseHeaders == null) { 
		        responseHeaders = new Series<Header>(Header.class); 
		        getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders); 
		    } 
			responseHeaders.add("Access-Control-Allow-Origin", "*");

		    Map<String, String> params = new HashMap<String, String>();
			byte[] speech = IOUtils.toByteArray(param.getStream());
			params.put("lang"  , getQueryValue("lang"  ));
			params.put("format", getQueryValue("format"));
			params.put("mode", getQueryValue("mode"));
			return getRepresentation(speech, params);
		} catch (Exception e) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
	}
	
	/**
	 * Obtem JSON.
	 * @param speech Dados do arquivo de midia a ser transformado.
	 * @param params Parametros para obtencao do JSON.
	 * @return Resposta (JSON).
	 */
	protected Representation getRepresentation(byte[] speech, Map<String, String> params) {
		try {
			Representation result = null;
			if (speech == null || speech.length == 0) {
				throw new NullPointerException("Attribute \"speech\" is required.");
			}
			JSONObject data = getJsonData(speech, params);
			result = new JsonRepresentation(data);
			result.setMediaType(MediaType.APPLICATION_JSON);
			return result;
		} catch (Exception e) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
	}
	
	/**
	 * Obtem dados JSON.
	 * @param speech Dados do arquivo de midia a ser transformado.
	 * @param params Parametros para obtencao do JSON.
	 * @return Dados em JSON.
	 */
	protected JSONObject getJsonData(byte[] speech, Map<String, String> params) throws Exception {
		String lang = params.get("lang");
		String format = params.get("format");
		String mode = params.get("mode");
		return stt.convert(speech, lang, format, mode);
	}

	@Override
	protected JSONObject getJsonData(Map<String, String> params) throws Exception {
		throw new NullPointerException("Attribute \"speech\" is required.");
	}

}
