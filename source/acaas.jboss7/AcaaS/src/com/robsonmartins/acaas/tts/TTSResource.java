package com.robsonmartins.acaas.tts;

import java.util.Map;

import org.json.JSONObject;

import com.robsonmartins.acaas.JsonResource;

/**
 * Servico REST para transformacao de Texto em Fala.
 * <p/>
 * <code>text</code> - Texto a ser convertido.<br/>
 * <code>lang</code> - Idioma do texto.<br/>
 * <code>format</code> - Formato de arquivo desejado para os arquivos de midia.<br/>
 *   
 * @author Robson Martins (robson@robsonmartins.com) 
 */
public class TTSResource extends JsonResource {
	
	/** Engine de transformacao de Texto para Fala. */ 
	private static TextToSpeech tts = null;
	
	/** 
	 * Construtor.<br/>
	 * Usa implementacao {@link GoogleTextToSpeech}.
	 */
	public TTSResource() {
		super();
		// usa implementacao GoogleTextToSpeech
		tts = new GoogleTextToSpeech(fetcher);
	}
	
	@Override
	protected JSONObject getJsonData(Map<String, String> params) throws Exception {
		String text = params.get("text");
		String lang = params.get("lang");
		String format = params.get("format");
		return tts.convert(text, lang, format);
	}
}
