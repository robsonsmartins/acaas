package com.robsonmartins.acaas.tts;

import org.json.JSONObject;

/**
 * Interface para Engine de transformacao de Texto para Fala. 
 * @author Robson Martins (robson@robsonmartins.com) 
 */
public interface TextToSpeech {
	
	/**
	 * Converte um texto para fala.
	 * @param text Texto a ser convertido.
	 * @param lang Idioma do texto.
	 * @param format Formato desejado para arquivos de midia.
	 * @return Objeto JSON que representa o Texto em Fala.
	 * @throws Exception
	 */
	public JSONObject convert(String text, String lang, String format) throws Exception;
	
	/**
	 * Converte um texto para fala.
	 * @param text Texto a ser convertido.
	 * @return Objeto JSON que representa o Texto em Fala.
	 * @throws Exception
	 */
	public JSONObject convert(String text) throws Exception;
	
	/**
	 * Retorna os dados de um arquivo de midia.
	 * @param text Texto a ser convertido.
	 * @param lang Idioma.
	 * @param format Formato do arquivo de midia.
	 * @return Dados do arquivo de midia.
	 * @throws Exception
	 */
	public byte[] getMedia(String text, String lang, String format) throws Exception;
	
	/**
	 * Retorna os dados de um arquivo de midia.
	 * @param text Texto a ser convertido.
	 * @return Dados do arquivo de midia.
	 * @throws Exception
	 */
	public byte[] getMedia(String text) throws Exception;

}
