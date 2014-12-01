package com.robsonmartins.acaas.sql;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.robsonmartins.acaas.t2libras.bean.PalavraBean;

/**
 * Classe DAO (Data Access Object) para manipulacao de dados do servico
 *   de transformacao para Libras. 
 * @author Robson Martins (robson@robsonmartins.com) 
 *
 */
public class LibrasDAO extends JdbcDAO {

	/* Queries para a base de dados acaas. */
	
	private static final String SQL_SELECT_LIBRAS_PALAVRA =
		"SELECT l.* FROM libras l" +
		" WHERE l.palavra = ?" +
		" ORDER BY l.palavra";
	
	private static final String SQL_SELECT_SINONIMO_PALAVRA =
		"SELECT l.* FROM libras l, sinonimos s" +
		" WHERE s.sinonimo = ? AND l.palavra = s.palavra" +
		" ORDER BY l.palavra";
	
	private static final String SQL_SELECT_VERBO_PALAVRA =
		"SELECT l.*, v.conjugacao, v.pessoa, v.tempo FROM libras l, verbos v" +
		" WHERE v.conjugacao = ? AND l.palavra = v.verbo" +
		" ORDER BY l.palavra";
	
	private static final String SQL_SELECT_VERBO_SINONIMO =
		"SELECT l.*, v.conjugacao, v.pessoa, v.tempo FROM libras l, sinonimos s, verbos_sinonimos v" +
		" WHERE v.conjugacao = ? AND l.palavra = s.palavra AND s.sinonimo = v.verbo" +
		" ORDER BY l.palavra";
	
	private static final String SQL_SELECT_INFORMAL_PALAVRA =
		"SELECT l.* FROM libras l, informal i" +
		" WHERE i.sinonimo = ? AND l.palavra = i.palavra" +
		" ORDER BY l.palavra";
	
	private static final String SQL_SELECT_VERBO_INFORMAL =
		"SELECT l.*, v.conjugacao, v.pessoa, v.tempo FROM libras l, informal i, verbos_informal v" +
		" WHERE v.conjugacao = ? AND l.palavra = i.palavra AND i.sinonimo = v.verbo" +
		" ORDER BY l.palavra";
	
	private static final String SQL_SELECT_IMAGEM =
		"SELECT jpg AS data FROM imagens WHERE id = ?";
	
	private static final String SQL_SELECT_VIDEO_MP4 =
		"SELECT mp4 AS data FROM videos WHERE id = ?";
	
	private static final String SQL_SELECT_VIDEO_OGG =
		"SELECT ogg AS data FROM videos WHERE id = ?";
	
	private static final String SQL_SELECT_IMAGEM_PALAVRA =
		"SELECT i.jpg AS data FROM imagens i, libras l" +
		" WHERE l.palavra = ? AND i.id = l.midia";
	
	private static final String SQL_SELECT_VIDEO_MP4_PALAVRA =
		"SELECT v.mp4 AS data FROM videos v, libras l" +
		" WHERE l.palavra = ? AND v.id = l.midia";
	
	private static final String SQL_SELECT_VIDEO_OGG_PALAVRA =
		"SELECT v.ogg AS data FROM videos v, libras l" +
		" WHERE l.palavra = ? AND v.id = l.midia";
	
	/* Conexao JDBC. */
	private Connection connection = null;
	/* URL JDBC para o banco de dados. */
	private String sqlUrl = null;
	/* Nome de Usuario do banco de dados. */
	private String sqlUser = null;
	/* Senha do Usuario do banco de dados. */
	private String sqlPassword = null;
	
	/** Construtor. */
	public LibrasDAO() { }

	/**
	 * Construtor.
	 * @param connection Conexao JDBC a utilizar.
	 */
	public LibrasDAO(Connection connection) { 
		setConnection(connection);
	}

	/**
	 * Construtor.
	 * @param connection Conexao JDBC a utilizar.
	 * @param url URL JDBC para o banco de dados.
	 * @param user Nome de Usuario do banco de dados.
	 * @param pwd Senha do Usuario do banco de dados.
	 * @throws Exception
	 */
	public LibrasDAO(Connection connection, String url, 
			String user, String pwd) throws Exception {
		
		setConnection(connection);
		setConnection(open(url, user, pwd));
	}

	/**
	 * Construtor.
	 * @param url URL JDBC para o banco de dados.
	 * @param user Nome de Usuario do banco de dados.
	 * @param pwd Senha do Usuario do banco de dados.
	 * @throws Exception
	 */
	public LibrasDAO(String url, 
			String user, String pwd) throws Exception {
		
		setConnection(open(url, user, pwd));
	}
	
	/**
	 * Retorna a conexao JDBC.
	 * @return Conexao JDBC.
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * Configura a conexao JDBC.
	 * @param connection Conexao JDBC.
	 * @return Conexao JDBC.
	 */
	public Connection setConnection(Connection connection) {
		this.connection = connection;
		return this.connection;
	}

	/**
	 * Abre a conexao JDBC.
	 * @param url URL JDBC para o banco de dados.
	 * @param user Nome de Usuario do banco de dados.
	 * @param pwd Senha do Usuario do banco de dados.
	 * @return Conexao JDBC.
	 * @throws Exception
	 */
	public Connection open(String url, String user, String pwd) throws Exception {
		sqlUrl = url;
		sqlUser = user;
		sqlPassword = pwd;
		return open();
	}
	
	/**
	 * Abre (ou reabre, se necessario) a conexao JDBC.
	 * @return Conexao JDBC.
	 * @throws Exception
	 */
	public Connection open() throws Exception {
		return openConnection(connection, sqlUrl, sqlUser, sqlPassword);
	}

	/**
	 * Fecha a conexao JDBC.
	 * @throws Exception
	 */
	public void close() throws Exception {
		closeConnection(connection);
		connection = null;
	}

	/**
	 * Busca uma palavra no Dicionario de Libras. 
	 * @param palavra Palavra (ou expressao) a ser procurada.
	 * @return Objeto {@link PalavraBean}, ou <code>null</code> se nao encontrada.
	 * @throws Exception
	 */
	public PalavraBean getPalavra(String palavra) throws Exception {
		if (palavra == null) {
			throw new NullPointerException("Null 'palavra'.");
		}
		List<PalavraBean> palavras = getPalavrasByQuery(palavra, SQL_SELECT_LIBRAS_PALAVRA, 1);
		return (palavras.size() != 0) ? palavras.get(0) : null;
	}

	/**
	 * Busca a primeira ocorrencia da palavra no Dicionario de Sinonimos. 
	 * @param sinonimo Palavra (ou expressao) a ser procurada.
	 * @return Objeto {@link PalavraBean}, ou <code>null</code> se nao encontrada.
	 * @throws Exception
	 */
	public PalavraBean getPalavraBySinonimo(String sinonimo) throws Exception {
		if (sinonimo == null) {
			throw new NullPointerException("Null 'sinonimo'.");
		}
		List<PalavraBean> palavras = getPalavrasByQuery(sinonimo, SQL_SELECT_SINONIMO_PALAVRA, 1);
		return (palavras.size() != 0) ? palavras.get(0) : null;
	}

	/**
	 * Busca todas ocorrencias da palavra no Dicionario de Sinonimos. 
	 * @param sinonimo Palavra (ou expressao) a ser procurada.
	 * @return Lista de palavras.
	 * @throws Exception
	 */
	public List<PalavraBean> getPalavrasBySinonimo(String sinonimo) throws Exception {
		if (sinonimo == null) {
			throw new NullPointerException("Null 'sinonimo'.");
		}
		return getPalavrasByQuery(sinonimo, SQL_SELECT_SINONIMO_PALAVRA, 0);
	}
	
	/**
	 * Busca a primeira ocorrencia da conjugacao de verbo no Dicionario de Libras. 
	 * @param conjugacao Palavra (ou expressao) a ser procurada.
	 * @return Objeto {@link PalavraBean}, ou <code>null</code> se nao encontrada.
	 * @throws Exception
	 */
	public PalavraBean getPalavraByConjugacao(String conjugacao) throws Exception {
		if (conjugacao == null) {
			throw new NullPointerException("Null 'conjugacao'.");
		}
		List<PalavraBean> palavras = getPalavrasByQuery(conjugacao, SQL_SELECT_VERBO_PALAVRA, 1);
		return (palavras.size() != 0) ? palavras.get(0) : null;
	}

	/**
	 * Busca todas ocorrencias da conjugacao de verbo no Dicionario de Libras. 
	 * @param conjugacao Palavra (ou expressao) a ser procurada.
	 * @return Lista de palavras.
	 * @throws Exception
	 */
	public List<PalavraBean> getPalavrasByConjugacao(String conjugacao) throws Exception {
		if (conjugacao == null) {
			throw new NullPointerException("Null 'conjugacao'.");
		}
		return getPalavrasByQuery(conjugacao, SQL_SELECT_VERBO_PALAVRA, 0);
	}
	
	/**
	 * Busca a primeira ocorrencia da conjugacao de verbo no Dicionario de Sinonimos. 
	 * @param conjugacao Palavra (ou expressao) a ser procurada.
	 * @return Objeto {@link PalavraBean}, ou <code>null</code> se nao encontrada.
	 * @throws Exception
	 */
	public PalavraBean getPalavraByConjugacaoSinonimo(String conjugacao) throws Exception {
		if (conjugacao == null) {
			throw new NullPointerException("Null 'conjugacao'.");
		}
		List<PalavraBean> palavras =  getPalavrasByQuery(conjugacao, SQL_SELECT_VERBO_SINONIMO, 1);
		return (palavras.size() != 0) ? palavras.get(0) : null;
	}

	/**
	 * Busca todas ocorrencias da conjugacao de verbo no Dicionario de Sinonimos. 
	 * @param conjugacao Palavra (ou expressao) a ser procurada.
	 * @return Lista de palavras.
	 * @throws Exception
	 */
	public List<PalavraBean> getPalavrasByConjugacaoSinonimo(String conjugacao) throws Exception {
		if (conjugacao == null) {
			throw new NullPointerException("Null 'conjugacao'.");
		}
		return getPalavrasByQuery(conjugacao, SQL_SELECT_VERBO_SINONIMO, 0);
	}

	/**
	 * Busca a primeira ocorrencia da palavra no Dicionario Informal de Sinonimos. 
	 * @param sinonimo Palavra (ou expressao) a ser procurada.
	 * @return Objeto {@link PalavraBean}, ou <code>null</code> se nao encontrada.
	 * @throws Exception
	 */
	public PalavraBean getPalavraBySinonimoInformal(String sinonimo) throws Exception {
		if (sinonimo == null) {
			throw new NullPointerException("Null 'sinonimo'.");
		}
		List<PalavraBean> palavras = getPalavrasByQuery(sinonimo, SQL_SELECT_INFORMAL_PALAVRA, 1);
		return (palavras.size() != 0) ? palavras.get(0) : null;
	}

	/**
	 * Busca todas ocorrencias da palavra no Dicionario Informal de Sinonimos. 
	 * @param sinonimo Palavra (ou expressao) a ser procurada.
	 * @return Lista de palavras.
	 * @throws Exception
	 */
	public List<PalavraBean> getPalavrasBySinonimoInformal(String sinonimo) throws Exception {
		if (sinonimo == null) {
			throw new NullPointerException("Null 'sinonimo'.");
		}
		return getPalavrasByQuery(sinonimo, SQL_SELECT_INFORMAL_PALAVRA, 0);
	}
	
	/**
	 * Busca a primeira ocorrencia da conjugacao de verbo no Dicionario Informal de Sinonimos. 
	 * @param conjugacao Palavra (ou expressao) a ser procurada.
	 * @return Objeto {@link PalavraBean}, ou <code>null</code> se nao encontrada.
	 * @throws Exception
	 */
	public PalavraBean getPalavraByConjugacaoSinonimoInformal(String conjugacao) throws Exception {
		if (conjugacao == null) {
			throw new NullPointerException("Null 'conjugacao'.");
		}
		List<PalavraBean> palavras =  getPalavrasByQuery(conjugacao, SQL_SELECT_VERBO_INFORMAL, 1);
		return (palavras.size() != 0) ? palavras.get(0) : null;
	}

	/**
	 * Busca todas ocorrencias da conjugacao de verbo no Dicionario Informal de Sinonimos. 
	 * @param conjugacao Palavra (ou expressao) a ser procurada.
	 * @return Lista de palavras.
	 * @throws Exception
	 */
	public List<PalavraBean> getPalavrasByConjugacaoSinonimoInformal(String conjugacao) throws Exception {
		if (conjugacao == null) {
			throw new NullPointerException("Null 'conjugacao'.");
		}
		return getPalavrasByQuery(conjugacao, SQL_SELECT_VERBO_INFORMAL, 0);
	}

	/**
	 * Retorna os dados binarios de uma midia.
	 * @param id ID do registro.
	 * @param format Formato desejado para a midia (OGG [default], MP4 ou JPG).
	 * @return Dados binarios.
	 * @throws Exception
	 */
	public byte[] getMedia(String id, String format) throws Exception {
		if (id == null) {
			throw new NullPointerException("Null 'id'.");
		}
		if (format == null) { format = ""; }
		if (format.equalsIgnoreCase("JPG")) {
			return getDataByQuery(id, SQL_SELECT_IMAGEM);
		} else if (format.equalsIgnoreCase("MP4")) {
			return getDataByQuery(id, SQL_SELECT_VIDEO_MP4);
		} else /* OGG */ {
			return getDataByQuery(id, SQL_SELECT_VIDEO_OGG);
		}
	}

	/**
	 * Busca os dados binarios da midia associada a uma palavra (ou expressao). 
	 * @param palavra Palavra (ou expressao) a ser procurada.
	 * @param format Formato desejado para a midia (OGG [default], MP4 ou JPG).
	 * @return Dados binarios da midia.
	 * @throws Exception
	 */
	public byte[] getMediaByPalavra(String palavra, String format) throws Exception {
		if (palavra == null) {
			throw new NullPointerException("Null 'palavra'.");
		}
		if (format == null) { format = ""; }
		if (format.equalsIgnoreCase("JPG")) {
			return getDataByQuery(palavra, SQL_SELECT_IMAGEM_PALAVRA);
		} else if (format.equalsIgnoreCase("MP4")) {
			return getDataByQuery(palavra, SQL_SELECT_VIDEO_MP4_PALAVRA);
		} else /* OGG */ {
			return getDataByQuery(palavra, SQL_SELECT_VIDEO_OGG_PALAVRA);
		}
	}
	
	/**
	 * Retorna dados binarios (BLOB) a partir de uma query.
	 * @param id ID do registro.
	 * @param query String SQL contendo a query a ser utilizada.
	 * @return Dados binarios.
	 * @throws Exception
	 */
	private byte[] getDataByQuery(String id, String query) throws Exception {
		connection = open();
		PreparedStatement consulta = prepareStatement(connection, query);
		consulta.setString(1, id);
		ResultSet resultSet = executeQuery(consulta);
		if (resultSet == null || !resultSet.first()) { return null; }
		Blob blob = resultSet.getBlob("data");
		byte[] result = IOUtils.toByteArray(blob.getBinaryStream());
		blob.free();
		return result;
	}
	
	/**
	 * Busca todas ocorrencias de uma palavra (ou expressao).
	 * @param palavra Palavra (ou expressao) a ser procurada.
	 * @param query String SQL contendo a query a ser utilizada.
	 * @param maxResults Numero maximo de resultados a retornar (0 = todos).
	 * @return Lista de palavras.
	 * @throws Exception
	 */
	private List<PalavraBean> getPalavrasByQuery(String palavra, 
			String query, long maxResults) throws Exception {
		
		List<PalavraBean> result = new ArrayList<PalavraBean>();
		connection = open();
		PreparedStatement consulta = prepareStatement(connection, query);
		consulta.setString(1, palavra.toUpperCase());
		ResultSet resultSet = executeQuery(consulta);
		if (resultSet == null || !resultSet.first()) { return result; }
		long idxResult = 0;
		do {
			PalavraBean palavraItem = getPalavraByResultSet(resultSet);
			result.add(palavraItem);
			idxResult++;
		} while (resultSet.next() && (idxResult < maxResults || maxResults <= 0));
		return result;
	}
	
	/**
	 * Retorna uma palavra a partir da posicao (cursor) corrente em um {@link ResultSet}.
	 * @param resultSet Resultado de uma query.
	 * @return Palavra.
	 * @throws Exception
	 */
	private PalavraBean getPalavraByResultSet(ResultSet resultSet) throws Exception {
		PalavraBean result = new PalavraBean();
		result.setPalavra   (resultSet.getString("palavra"   ));
		result.setDescricao (resultSet.getString("descricao" ));
		result.setClasse    (resultSet.getString("classe"    ));
		result.setOrigem    (resultSet.getString("origem"    ));
		result.setMedia     (resultSet.getString("midia"     ));
		result.setPortugues (resultSet.getString("portugues" ));
		result.setLibras    (resultSet.getString("libras"    ));
		try {
			result.setConjugacao(resultSet.getString("conjugacao"));
			result.setPessoa    (resultSet.getString("pessoa"    ));
			result.setTempo     (resultSet.getString("tempo"     ));
		} catch (Exception e) { }
		return result;
	}
}
