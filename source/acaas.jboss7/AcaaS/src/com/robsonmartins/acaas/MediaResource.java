package com.robsonmartins.acaas;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.header.Header;
import org.restlet.engine.util.Base64;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.util.Series;

/**
 * Classe abstrata para Servico REST de obtencao de arquivos de midia.
 * <p/>
 * <code>id</code> - ID do arquivo de midia a ser obtido.<br/>
 *   
 * @author Robson Martins (robson@robsonmartins.com) 
 */
public abstract class MediaResource extends AbstractResource {

	/**
	 * Classe que representa uma midia para streaming HTTP. 
	 */
	private class MediaRepresentation extends OutputRepresentation {
		private Map<String, String> params;
		public MediaRepresentation(MediaType mediaType, Map<String, String> params) {
			super(mediaType);
			this.params = params;
		}
		@Override
		public void write(OutputStream os) throws IOException {
			try {
				os.write(getMediaData(params));
				os.flush();
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
	}	

	/** Construtor. */
	public MediaResource() { super(); }
	
	/**
	 * Permite somente metodo GET e OPTIONS.
	 * @return False.
	 */
	@Override
	public Set<Method> getAllowedMethods() {
		Set<Method> methods = new TreeSet<Method>();
		methods.add(Method.OPTIONS);
		methods.add(Method.GET);
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
		    responseHeaders.add("Access-Control-Allow-Methods", "OPTIONS,GET");
		} catch (Exception e) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
	}

	/**
	 * Responde requsicoes via metodo GET.
	 * @return Resposta (MEDIA/BINARY).
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
			
			String id = getAttribute("id");
			if (id == null || id.equals("")) {
				throw new NullPointerException("Attribute \"id\" is required.");
			}
			ByteArrayInputStream is = 
				new ByteArrayInputStream(Base64.decode(id));
			Properties prop = new Properties();
			prop.load(is);
			Map<String, String> params = new HashMap<String, String>();
			for (final String name: prop.stringPropertyNames()) {
			    params.put(name, prop.getProperty(name));
			}
			return getRepresentation(params);
		} catch (Exception e) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
	}
	
	/**
	 * Obtem tipo de midia.
	 * @param format Formato de arquivo desejado para o arquivo de midia.
	 * @return Tipo de midia.
	 */
	protected abstract MediaType getMediaType(String format);
	
	/**
	 * Obtem dados (binario) do arquivo de midia.
	 * @param params Parametros para obtencao da midia.
	 * @return Dados da midia.
	 */
	protected abstract byte[] getMediaData(Map<String, String> params) throws Exception;

	/**
	 * Obtem midia.
	 * @param params Parametros para obtencao da midia.<br/>
	 *   Deve conter obrigatoriamente a chave:<br/>
	 *   <ul><li><code>filename</code> - Nome do arquivo de midia.</li></ul>
	 * @return Resposta (MEDIA/BINARY).
	 */
	@Override
	protected Representation getRepresentation(Map<String, String> params) {
		Representation result = null;
		try {
			if (params == null || params.size() == 0) {
				throw new NullPointerException("Attribute \"filename\" is required.");
			}
			String filename = params.get("filename");
			String format = params.get("format");
			if (filename == null || filename.equals("")) {
				throw new NullPointerException("Attribute \"filename\" is required.");
			}
			MediaType mediaType = getMediaType(format);
			result = new MediaRepresentation(mediaType, params);
		} catch (Exception e) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
		return result;
	}
}
