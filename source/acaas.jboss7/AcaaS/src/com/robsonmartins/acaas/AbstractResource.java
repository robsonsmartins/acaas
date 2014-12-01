package com.robsonmartins.acaas;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.http.impl.client.DefaultHttpClient;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.header.Header;
import org.restlet.representation.Representation;
import org.restlet.resource.Options;
import org.restlet.resource.ServerResource;
import org.restlet.util.Series;

/**
 * Classe abstrata para Servico REST.
 *   
 * @author Robson Martins (robson@robsonmartins.com) 
 */
public abstract class AbstractResource extends ServerResource {

	/** Nome do arquivo de propriedades da aplicacao. */
	private static final String ACAAS_PROPERTIES_FILENAME = "/acaas.properties";
	/** Propriedades da aplicacao. */
	protected static Properties properties = null;
	/** Cliente HTTP. */
	protected static DefaultHttpClient fetcher = null;

	
	/** Construtor. */
	public AbstractResource() {
		super();
		// carrega arquivo de propriedades
		properties = new Properties();
		try {
			fetcher = new DefaultHttpClient();
			InputStream inStream = getClass().getResourceAsStream(ACAAS_PROPERTIES_FILENAME);
			properties.load(inStream);
		} catch (Exception e) { }
	}
	
	/**
	 * Permite metodo OPTIONS.
	 * @return False.
	 */
	@Override
	public Set<Method> getAllowedMethods() {
		Set<Method> methods = new TreeSet<Method>();
		methods.add(Method.OPTIONS);
		return methods;
	}
	
	/**
	 * Trata requsicoes via metodo OPTIONS.
	 * @param entity Corpo da Requisicao.
	 */
	@Options
	public void doOptions(Representation entity) {
		try {
			@SuppressWarnings("unchecked")
		    Series<Header> responseHeaders = 
		    	(Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers"); 
		    if (responseHeaders == null) { 
		        responseHeaders = new Series<Header>(Header.class); 
		        getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders); 
		    } 
		    responseHeaders.add("Access-Control-Allow-Origin", "*"); 
		    responseHeaders.add("Access-Control-Allow-Methods", "OPTIONS");
		    responseHeaders.add("Access-Control-Allow-Headers", "Content-Type"); 
		    responseHeaders.add("Access-Control-Allow-Credentials", "false"); 
		    responseHeaders.add("Access-Control-Max-Age", "60"); 
		} catch (Exception e) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
	} 	
	
	/**
	 * Obtem representacao de dados.
	 * @param params Parametros para obtencao dos dados.
	 * @return Resposta.
	 */
	protected abstract Representation getRepresentation(Map<String, String> params);
}
