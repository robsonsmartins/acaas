package com.robsonmartins.acaas.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 * Classe DAO (Data Access Object) para manipulacao de dados via JDBC. 
 * @author Robson Martins (robson@robsonmartins.com) 
 *
 */
public class JdbcDAO {
	
	/** Timeout (em segundos) para considerar uma conexao JDBC fechada. */
	protected static final int JDBC_SQL_VALID_TIMEOUT = 10;

	/**
	 * Abre uma conexao JDBC.
	 * @param url URL JDBC para o banco de dados.
	 * @param user Nome de Usuario do banco de dados.
	 * @param pwd Senha do Usuario do banco de dados.
	 * @return Conexao JDBC.
	 * @throws Exception
	 */
	protected static Connection openConnection(String url, 
			String user, String pwd) throws Exception {
		
		return DriverManager.getConnection(url, user, pwd);
	}

	/**
	 * Verifica se ha' uma conexao JDBC aberta, caso contrario abre uma nova.
	 * @param connection Conexao JDBC a verificar.
	 * @param url URL JDBC para o banco de dados.
	 * @param user Nome de Usuario do banco de dados.
	 * @param pwd Senha do Usuario do banco de dados.
	 * @return Conexao JDBC.
	 * @throws Exception
	 */
	protected static Connection openConnection(Connection connection, String url, 
			String user, String pwd) throws Exception {
		
		if (connection == null || !connection.isValid(JDBC_SQL_VALID_TIMEOUT)) {
			closeConnection(connection);
			return openConnection(url, user, pwd);
		} else {
			return connection;
		}
	}
	
	/**
	 * Abre uma conexao JDBC atraves de JNDI.
	 * @param url URL JNDI.
	 * @return Conexao JDBC.
	 * @throws Exception
	 */
	protected static Connection openConnectionByJndi(String url) throws Exception {
		Context context = new InitialContext();
		DataSource ds = (DataSource) context.lookup(url);
		return ds.getConnection();
	}
	
	/**
	 * Verifica se ha' uma conexao JDBC aberta, caso contrario abre uma nova via JNDI.
	 * @param connection Conexao JDBC a verificar.
	 * @param url URL JNDI.
	 * @return Conexao JDBC.
	 * @throws Exception
	 */
	protected static Connection openConnectionByJndi(
			Connection connection, String url) throws Exception {
		
		if (connection == null || !connection.isValid(JDBC_SQL_VALID_TIMEOUT)) {
			closeConnection(connection);
			return openConnectionByJndi(url);
		} else {
			return connection;
		}
	}

	/**
	 * Fecha uma conexao JDBC aberta.
	 * @param connection Conexao JDBC.
	 */
	protected static void closeConnection(Connection connection) {
		if (connection != null) {
			try { connection.close(); } catch (Exception e) { }
		}
	}
	
	/**
	 * Cria um {@link PreparedStatement}. 
	 * @param connection Conexao JDBC.
	 * @param statement String SQL.
	 * @return Objeto {@link PreparedStatement}.
	 * @throws Exception
	 */
	protected static PreparedStatement prepareStatement(
			Connection connection, String statement) throws Exception {
		
		return connection.prepareStatement(statement);
	}
	
	/**
	 * Executa uma query (SELECT) contida em um {@link PreparedStatement}.
	 * @param statement {@link PreparedStatement} SQL.
	 * @return Resultado da query (objeto {@link ResultSet}).
	 * @throws Exception
	 */
	protected static ResultSet executeQuery(
			PreparedStatement statement) throws Exception {
		
		return statement.executeQuery();
	}

	/**
	 * Executa uma query (INSERT, UPDATE, etc.) contida em um {@link PreparedStatement}.
	 * @param statement {@link PreparedStatement} SQL.
	 * @return True se foi bem sucedido, false se erro.
	 * @throws Exception
	 */
	protected static boolean execute(
			PreparedStatement statement) throws Exception {
		
		return statement.execute();
	}

}
