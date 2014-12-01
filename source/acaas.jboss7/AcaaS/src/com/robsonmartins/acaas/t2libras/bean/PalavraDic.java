package com.robsonmartins.acaas.t2libras.bean;

/**
 * Enumera os dicionarios disponiveis para auxiliar na
 *   transformacao de Texto para Libras.
 * @author Robson Martins (robson@robsonmartins.com) 
 */
public enum PalavraDic {
	/** Dicionario de Libras do
	 *  <a target="_blank" href="http://www.acessobrasil.org.br/libras/">Acessibilidade Brasil</a>. */
	DIC_LIBRAS          ("Dicion\u00E1rio de Libras"), 
	/** Conjugacoes dos Verbos do Dicionario de Libras, 
	 *  obtidas pelo Dicionario Online de Portugues <a target="_blank" href="http://www.dicio.com.br/">Dicio</a>. */
	DIC_VERBOS          ("Conjuga\u00E7\u00E3o de Verbos do Dicion\u00E1rio de Libras"),
	/** Sinonimos das Palavras do Dicionario de Libras,
	 *  obtidos pelo Dicionario Online de Portugues <a target="_blank" href="http://www.dicio.com.br/">Dicio</a>. */
	DIC_SINONIMOS       ("Dicion\u00E1rio de Sin\u00F4nimos"),
	/** Conjugacoes dos Verbos do Dicionario de Sinonimos, 
	 *  obtidas pelo Dicionario Online de Portugues <a target="_blank" href="http://www.dicio.com.br/">Dicio</a>. */
	DIC_SINONIMOS_VERBOS("Conjuga\u00E7\u00E3o de Verbos do Dicion\u00E1rio de Sin\u00F4nimos"),
	/** Sinonimos das Palavras do Dicionario de Libras,
	 *  obtidos pelo Dicionario Informal <a target="_blank" href="http://www.dicionarioinformal.com.br/">Dicionario inFormal</a>
	 */
	DIC_INFORMAL        ("Dicion\u00E1rio Informal de Sin\u00F4nimos"), 
	/** Conjugacoes dos Verbos do Dicionario Informal, 
	 *  obtidas pelo Dicionario Online de Portugues <a target="_blank" href="http://www.dicio.com.br/">Dicio</a>. */
	DIC_INFORMAL_VERBOS ("Conjuga\u00E7\u00E3o de Verbos do Dicion\u00E1rio Informal de Sin\u00F4nimos");
	
	private String desc;

	private PalavraDic(String desc) {
		this.desc = desc;
	}
	
	@Override
	public String toString() {
		return desc;
	}

}
