package com.robsonmartins.acaas.t2libras;

import java.util.Map;

import org.json.JSONObject;

import com.robsonmartins.acaas.JsonResource;

/**
 * Servico REST para transformacao de Texto em Libras.
 * <p/>
 * <code>text</code> - Texto a ser convertido.<br/>
 * <code>lang</code> - Idioma do texto.<br/>
 * <code>format</code> - Formato de arquivo desejado para os arquivos de midia.<br/>
 * <code>mode</code> - Modo de retorno JSON.<br/>
 *   
 * @author Robson Martins (robson@robsonmartins.com) 
 */
public class T2LibrasResource extends JsonResource {
	
	/** Engine de transformacao de Texto para Libras. */ 
	private static TextToLibras t2libras = null;
	
	/** 
	 * Construtor.<br/>
	 * Usa implementacao {@link AccBrasilTextToLibras}.
	 */
	public T2LibrasResource() {
		super();
		// usa implementacao AccBrasilTextToLibras
		t2libras = new AccBrasilTextToLibras(properties);
	}
	
	@Override
	protected JSONObject getJsonData(Map<String, String> params) throws Exception {
		String text = params.get("text");
		String lang = params.get("lang");
		String format = params.get("format");
		String mode = params.get("mode");
		return t2libras.convert(text, lang, format, mode);
	}
}
