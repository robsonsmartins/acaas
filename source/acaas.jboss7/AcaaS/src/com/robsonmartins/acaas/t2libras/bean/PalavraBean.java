package com.robsonmartins.acaas.t2libras.bean;

/**
 * Representa uma palavra nos dicionarios
 *   usados para transformacao de Texto para Libras.
 * @author Robson Martins (robson@robsonmartins.com) 
 */
public class PalavraBean {

	private String palavra    = "";
	private String descricao  = "";
	private String classe     = "";
	private String origem     = "";
	private String media      = "";
	private String portugues  = "";
	private String libras     = "";
	private String language   = "";
	private String format     = "";
	private String conjugacao = "";
	private String pessoa     = "";
	private String tempo      = "";

	/**
	 * Obtem Palavra.
	 * @return Palavra.
	 */
	public String getPalavra() {
		return palavra;
	}

	/**
	 * Configura Palavra.
	 * @param palavra Palavra.
	 */
	public void setPalavra(String palavra) {
		this.palavra = palavra;
	}

	/**
	 * Obtem Descricao.
	 * @return Descricao da palavra.
	 */
	public String getDescricao() {
		return descricao;
	}

	/**
	 * Configura Descricao.
	 * @param descricao Descricao da palavra.
	 */
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	/**
	 * Obtem Classe Gramatical.
	 * @return Classe Gramatical da Palavra.
	 */
	public String getClasse() {
		return classe;
	}

	/**
	 * Configura Classe Gramatical.
	 * @param classe Classe Gramatical da Palavra.
	 */
	public void setClasse(String classe) {
		this.classe = classe;
	}

	/**
	 * Obtem Origem da Palavra em Libras.
	 * @return Origem da Palavra em Libras.
	 */
	public String getOrigem() {
		return origem;
	}

	/**
	 * Configura Origem da Palavra em Libras.
	 * @param origem Origem da Palavra em Libras.
	 */
	public void setOrigem(String origem) {
		this.origem = origem;
	}

	/**
	 * Obtem ID do Arquivo de Midia.
	 * @return ID do Arquivo de Midia da Palavra.
	 */
	public String getMedia() {
		return media;
	}

	/**
	 * Configura ID do Arquivo de Midia.
	 * @param media ID do Arquivo de Midia da Palavra.
	 */
	public void setMedia(String media) {
		this.media = media;
	}

	/**
	 * Obtem Exemplo da Palavra em Portugues Escrito/Oral.
	 * @return Exemplo da Palavra em Portugues.
	 */
	public String getPortugues() {
		return portugues;
	}

	/**
	 * Configura Exemplo da Palavra em Portugues Escrito/Oral.
	 * @param portugues Exemplo da Palavra em Portugues.
	 */
	public void setPortugues(String portugues) {
		this.portugues = portugues;
	}

	/**
	 * Obtem Exemplo da Palavra em Libras.
	 * @return Exemplo da Palavra em Libras.
	 */
	public String getLibras() {
		return libras;
	}

	/**
	 * Configura Exemplo da Palavra em Libras.
	 * @param libras Exemplo da Palavra em Libras.
	 */
	public void setLibras(String libras) {
		this.libras = libras;
	}

	/**
	 * Obtem Idioma.
	 * @return Idioma da Palavra.
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Configura Idioma.
	 * @param language Idioma da Palavra.
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * Obtem Formato do Arquivo de Midia.
	 * @return Formato do Arquivo de Midia.
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * Configura Formato do Arquivo de Midia.
	 * @param format Formato do Arquivo de Midia.
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * Obtem uma Conjugacao da Palavra, se ela for Verbo.
	 * @return Conjugacao da Palavra.
	 */
	public String getConjugacao() {
		return conjugacao;
	}

	/**
	 * Configura uma Conjugacao da Palavra, se ela for Verbo.
	 * @param conjugacao Conjugacao da Palavra.
	 */
	public void setConjugacao(String conjugacao) {
		this.conjugacao = conjugacao;
	}

	/**
	 * Obtem a Pessoa de uma Conjugacao da Palavra, se ela for Verbo.
	 * @return Pessoa de uma Conjugacao da Palavra.
	 */
	public String getPessoa() {
		return pessoa;
	}

	/**
	 * Configura a Pessoa de uma Conjugacao da Palavra, se ela for Verbo.
	 * @param pessoa Pessoa de uma Conjugacao da Palavra.
	 */
	public void setPessoa(String pessoa) {
		this.pessoa = pessoa;
	}

	/**
	 * Obtem o Tempo Verbal de uma Conjugacao da Palavra, se ela for Verbo.
	 * @return Tempo Verbal de uma Conjugacao da Palavra.
	 */
	public String getTempo() {
		return tempo;
	}

	/**
	 * Configura o Tempo Verbal de uma Conjugacao da Palavra, se ela for Verbo.
	 * @param tempo Tempo Verbal de uma Conjugacao da Palavra.
	 */
	public void setTempo(String tempo) {
		this.tempo = tempo;
	}
}
