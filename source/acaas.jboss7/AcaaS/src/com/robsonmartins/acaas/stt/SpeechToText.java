package com.robsonmartins.acaas.stt;

import org.json.JSONObject;

/**
 * Interface para Engine de transformacao de Fala para Texto. 
 * @author Robson Martins (robson@robsonmartins.com) 
 */
public interface SpeechToText {

	/**
	 * Converte fala (audio) para Texto.
	 * @param speech Fala a ser convertida.
	 * @param lang Idioma da fala.
	 * @param format Formato dos dados de audio.
	 * @param mode Modo de retorno JSON. 
	 * @return Objeto JSON que representa o Texto.
	 * @throws Exception
	 */
	public JSONObject convert(byte[] speech, String lang, String format, String mode) throws Exception;
	
	/**
	 * Converte fala (audio) para Texto.
	 * @param speech Fala a ser convertida.
	 * @param lang Idioma da fala.
	 * @param format Formato dos dados de audio.
	 * @return Objeto JSON que representa o Texto.
	 * @throws Exception
	 */
	public JSONObject convert(byte[] speech, String lang, String format) throws Exception;

	/**
	 * Converte fala (audio) para Texto.
	 * @param speech Fala a ser convertida.
	 * @param mode Modo de retorno JSON. 
	 * @return Objeto JSON que representa o Texto.
	 * @throws Exception
	 */
	public JSONObject convert(byte[] speech, String mode) throws Exception;

	/**
	 * Converte fala (audio) para Texto.
	 * @param speech Fala a ser convertida.
	 * @return Objeto JSON que representa o Texto.
	 * @throws Exception
	 */
	public JSONObject convert(byte[] speech) throws Exception;
	
}
