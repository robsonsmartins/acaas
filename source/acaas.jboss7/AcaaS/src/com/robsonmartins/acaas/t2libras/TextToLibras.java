package com.robsonmartins.acaas.t2libras;

import org.json.JSONObject;

/**
 * Interface para Engine de transformacao de Texto para Libras. 
 * @author Robson Martins (robson@robsonmartins.com) 
 */
public interface TextToLibras {
	/**
	 * Converte um texto em linguagem falada para Libras.
	 * @param text Texto a ser convertido.
	 * @param lang Idioma do texto.
	 * @param format Formato desejado para arquivos de midia.
	 * @param mode Modo de retorno JSON. 
	 * @return Objeto JSON que representa o Texto em Libras.
	 * @throws Exception
	 */
	public JSONObject convert(String text, String lang, String format, String mode) throws Exception;
	
	/**
	 * Converte um texto em linguagem falada para Libras, 
	 *   usando idioma e formato de midia padroes.
	 * @param text Texto a ser convertido.
	 * @param mode Modo de retorno JSON. 
	 * @return Objeto JSON que representa o Texto em Libras.
	 * @throws Exception
	 */
	public JSONObject convert(String text, String mode) throws Exception;
	
	/**
	 * Converte um texto em linguagem falada para Libras, 
	 *   usando idioma, formato de midia e modo JSON padroes.
	 * @param text Texto a ser convertido.
	 * @return Objeto JSON que representa o Texto em Libras.
	 * @throws Exception
	 */
	public JSONObject convert(String text) throws Exception;
	
	/**
	 * Retorna os dados de um arquivo de midia.
	 * @param filename Nome do arquivo de midia (ID).
	 * @param lang Idioma.
	 * @param format Formato do arquivo de midia.
	 * @return Dados do arquivo de midia.
	 * @throws Exception
	 */
	public byte[] getMedia(String filename, String lang, String format) throws Exception;
	
	/**
	 * Retorna os dados de um arquivo de midia,
	 *   usando idioma e formato de midia padroes.
	 * @param filename Nome do arquivo de midia (ID).
	 * @return Dados do arquivo de midia.
	 * @throws Exception
	 */
	public byte[] getMedia(String filename) throws Exception;
}
