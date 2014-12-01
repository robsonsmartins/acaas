package com.robsonmartins.acaas.s2libras;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.robsonmartins.acaas.stt.GoogleSpeechToText;
import com.robsonmartins.acaas.stt.STTResource;
import com.robsonmartins.acaas.t2libras.AccBrasilTextToLibras;
import com.robsonmartins.acaas.t2libras.TextToLibras;

/**
 * Servico REST para transformacao de Fala em Libras.
 * <p/>
 * <code>speech (POST)</code> - Arquivo de audio a ser convertido.<br/>
 * <code>lang</code> - Idioma do texto.<br/>
 * <code>iformat</code> - Formato do arquivo de entrada (speech).<br/>
 * <code>oformat</code> - Formato de arquivo desejado para os arquivos de saida (midia).<br/>
 * <code>mode</code> - Modo de retorno JSON.<br/>
 *   
 * @author Robson Martins (robson@robsonmartins.com) 
 */
public class S2LibrasResource extends STTResource {
	
	/** Engine de transformacao de Texto para Libras. */ 
	protected static TextToLibras t2libras = null;
	
	/** 
	 * Construtor.<br/>
	 * Usa implementacoes {@link GoogleSpeechToText} e {@link AccBrasilTextToLibras}.
	 */
	public S2LibrasResource() {
		super();
		// usa implementacao AccBrasilTextToLibras
		t2libras = new AccBrasilTextToLibras(properties);
	}
	
	/**
	 * Obtem dados JSON.
	 * @param speech Dados do arquivo de midia a ser transformado.
	 * @param params Parametros para obtencao do JSON.
	 * @return Dados em JSON.
	 */
	protected JSONObject getJsonData(byte[] speech, Map<String, String> params) throws Exception {
		String lang = params.get("lang");
		String inFormat = params.get("iformat");
		String outFormat = params.get("oformat");
		String mode = params.get("mode");
		StringBuilder text = new StringBuilder();
		JSONObject json = stt.convert(speech, lang, inFormat);
		JSONArray items = null;
		if (json != null) {
			items = json.getJSONArray("items");
		}
		if (items != null && items.length() != 0) {
			for (int i = 0; i < items.length(); i++) {
				if (i > 0) {text.append(" "); }
				text.append(items.getString(i));
			}
		}
		return t2libras.convert(text.toString(), lang, outFormat, mode);
	}
}
