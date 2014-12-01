package com.robsonmartins.acaas.t2libras.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa uma busca de palavra ou expressao nos dicionarios
 *   usados para transformacao de Texto para Libras.
 * @author Robson Martins (robson@robsonmartins.com) 
 */
public class PalavraSearch {

	private List<String> expressao;
	private List<PalavraBean> possibilidades;
	private int first;
	private int last;
	private PalavraDic dic;
		
	/** Construtor. */
	public PalavraSearch() { 
		expressao = new ArrayList<String>();
		possibilidades = new ArrayList<PalavraBean>();
		first = -1;	last = -1;
		dic = PalavraDic.DIC_LIBRAS;
	}

	/**
	 * Obtem expressao de procura.
	 * @return Expressao de procura.
	 */
	public List<String> getExpressao() {
		return expressao;
	}

	/**
	 * Configura expressao de procura.
	 * @param expressao Expressao de procura.
	 */
	public void setExpressao(List<String> expressao) {
		this.expressao = expressao;
	}

	/**
	 * Obtem lista de palavras (possibilidades) encontradas.
	 * @return Lista de possibilidades.
	 */
	public List<PalavraBean> getPossibilidades() {
		return possibilidades;
	}

	/**
	 * Configura lista de palavras (possibilidades) encontradas.
	 * @param possibilidades Lista de possibilidades.
	 */
	public void setPossibilidades(List<PalavraBean> possibilidades) {
		this.possibilidades = possibilidades;
	}

	/**
	 * Obtem indice da primeira palavra a ser procurada, dentro da expressao.
	 * @return Indice da primeira palavra a ser procurada (inicial = 0).
	 */
	public int getFirst() {
		return first;
	}

	/**
	 * Configura indice da primeira palavra a ser procurada, dentro da expressao.
	 * @param first Indice da primeira palavra a ser procurada (inicial = 0).
	 */
	public void setFirst(int first) {
		this.first = first;
	}

	/**
	 * Obtem indice da ultima palavra a ser procurada, dentro da expressao.
	 * @return Indice da ultima palavra a ser procurada (inicial = 0).
	 */
	public int getLast() {
		return last;
	}

	/**
	 * Configura indice da ultima palavra a ser procurada, dentro da expressao.
	 * @param last Indice da ultima palavra a ser procurada (inicial = 0).
	 */
	public void setLast(int last) {
		this.last = last;
	}

	/**
	 * Obtem o dicionario usado para a busca.
	 * @return Dicionario usado para busca.
	 */
	public PalavraDic getDic() {
		return dic;
	}

	/**
	 * Configura o dicionario usado para a busca.
	 * @param dic Dicionario usado para busca.
	 */
	public void setDic(PalavraDic dic) {
		this.dic = dic;
	}
}
