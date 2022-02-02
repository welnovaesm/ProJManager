package br.com.proj.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransacaoDB {

    Logger logger;
    Connection connection = null;
    String url = "jdbc:mariadb://localhost:3306/projmanager";
    String user = "root";
    String password = "root";

    /**
     * conectar no banco de dados
     * 
     * @throws Exception
     */
    private void conectar() throws Exception {
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Não foi possivel realizar a conexão");
            throw e;
        }
    }

    /**
     * desconectar de forma segura do banco de dados
     * 
     * @throws Exception
     */
    private void desconectar() throws Exception {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Não foi possivel conectar ao DB " + e);
            throw e;
        }
    }

    /**
     * abrir transação no banco de dados. Pode somente ler e escrever no DB
     * 
     * @param reader
     * @throws Exception
     */
    public void abrirTrsansacao(boolean reader) throws Exception {
        if (connection == null) {
            conectar();
        }
        connection.setReadOnly(reader);

        if (connection != null && !connection.isClosed()) {
            connection.setAutoCommit(false);
        } else {
            throw new Exception("Não foi possível abrir a Transação");
        }
    }

    /**
     * fechar transação de forma segura
     * 
     * @throws Exception
     */
    public void fecharTransacao() throws Exception {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.commit();
                connection.setAutoCommit(true);
                desconectar();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Não foi possível fechar a transação" + e);
            throw e;
        }
    }

    /**
     * Criar instrução na conexão SQL com instruções de execução
     * 
     * @param sql
     * @return
     * @throws Exception
     */
    public Statement createStatement(String sql) throws Exception {
        if (connection != null && !connection.isClosed())
            return connection.createStatement();
        else
            throw new Exception("Não foi possível criar a instrução para o SQL");
    }

}
