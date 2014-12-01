package com.robsonmartins.acaas;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.header.Header;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.util.Series;

/**
 * Classe abstrata para Servico REST de transformacao de texto para JSON.
 * <p/>
 * <code>text</code> - Texto a ser transformado.<br/>
 * <code>lang</code> - Idioma do texto.<br/>
 * <code>format</code> - Formato de arquivo desejado para os arquivos de midia.<br/>
 * <code>mode</code> - Modo de retorno JSON.<br/>
 *   
 * @author Robson Martins (robson@robsonmartins.com) 
 */
public abstract class JsonResource extends AbstractResource {

	/** Construtor. */
	public JsonResource() {	super(); }
	
	/**
	 * Permite metodos OPTIONS, POST e GET.
	 * @return False.
	 */
	@Override
	public Set<Method> getAllowedMethods() {
		Set<Method> methods = new TreeSet<Method>();
		methods.add(Method.OPTIONS);
		methods.add(Method.GET);
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
		    responseHeaders.add("Access-Control-Allow-Methods", "OPTIONS,POST,GET");
		} catch (Exception e) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
	}
	
	/**
	 * Responde requsicoes via metodo POST.
	 * @param param Corpo da Requisicao (contem o parametro <code>text</code>).
	 * @return Resposta (JSON).
	 */
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
			params.put("text"  , param.getText());
			params.put("lang"  , getQueryValue("lang"  ));
			params.put("format", getQueryValue("format"));
			params.put("mode"  , getQueryValue("mode"  ));
			return getRepresentation(params);
		} catch (Exception e) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
	}
	
	/**
	 * Responde requsicoes via metodo GET.
	 * @return Resposta (JSON).
	 */
	@Get
	public Representation responseGet() {
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
			params.put("text"  , getQueryValue("text"  ));
			params.put("lang"  , getQueryValue("lang"  ));
			params.put("format", getQueryValue("format"));
			params.put("mode"  , getQueryValue("mode"  ));
			return getRepresentation(params);
		} catch (Exception e) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
	}
	
	/**
	 * Obtem dados JSON.
	 * @param params Parametros para obtencao do JSON.
	 * @return Dados em JSON.
	 */
	protected abstract JSONObject getJsonData(Map<String, String> params) throws Exception;
	
	/**
	 * Obtem JSON.
	 * @param params Parametros para obtencao do JSON.<br/>
	 *   Deve conter obrigatoriamente a chave:<br/>
	 *   <ul><li><code>text</code> - Texto a ser transformado.</li></ul>
	 * @return Resposta (JSON).
	 */
	@Override
	protected Representation getRepresentation(Map<String, String> params) {
		Representation result = null;
		try {
			if (params == null || params.size() == 0) {
				throw new NullPointerException("Attribute \"text\" is required.");
			}
			String text = params.get("text");
			if (text == null || text.equals("")) {
				throw new NullPointerException("Attribute \"text\" is required.");
			}
			JSONObject data = getJsonData(params);
			result = new JsonRepresentation(data);
			result.setMediaType(MediaType.APPLICATION_JSON);
		} catch (Exception e) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
		return result;
	}
}
