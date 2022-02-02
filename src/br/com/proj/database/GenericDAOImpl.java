package br.com.proj.database;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.com.proj.util.Coluna;
import br.com.proj.util.DatabaseException;
import br.com.proj.util.Tabela;

public class GenericDAOImpl<T, ID extends Serializable> implements GenericDAO<T, ID> {

    Logger logger;
    private Class<T> oClass;
    protected TransacaoDB transacaoDB;

    public TransacaoDB getTransacaoDB() {
        return transacaoDB;
    }

    public void setTransacaoDB(TransacaoDB transacaoDB) {
        this.transacaoDB = transacaoDB;
    }

    /**
     * Construtor
     */
    @SuppressWarnings("unchecked")
    public GenericDAOImpl() {
        oClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * Classe a ser persistida
     */
    public Class<T> getObjectClass() {
        return oClass;
    }

    /**
     * Pegar instancia da classe
     * 
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public T novaInstancia() throws InstantiationException, IllegalAccessException {
        return oClass.newInstance();
    }

    /**
     * incluir objeto no banco de dados
     */
    public T criar(T objeto) throws DatabaseException {
        Statement statement = null;

        try {
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("INSERT INTO ");
            sqlBuilder.append(getNomeTabela());
            sqlBuilder.append(criarComandoInsercao(objeto));

            System.out.println(sqlBuilder.toString());
            statement = transacaoDB.createStatement(sqlBuilder.toString());
            statement.execute(sqlBuilder.toString());

            return null;
        } catch (Exception e) {
            throw new DatabaseException(e, "Incapaz de executar o comando 'create' no banco de dados" + e.getMessage());
        } finally {
            try {
                if (statement != null)
                    statement.close();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error: ", e);
            }
        }

    }

    /**
     * listar objeto T no banco de dados
     */
    public T consultar(Integer id) throws DatabaseException {

        Statement statement = null;
        ResultSet resultSet = null;
        T objeto = null;

        try {
            StringBuilder sqlBuilder = new StringBuilder();

            sqlBuilder.append("SELECT * FROM ");
            sqlBuilder.append(getNomeTabela());
            sqlBuilder.append(" WHERE ID = ");
            sqlBuilder.append(id);

            System.out.println(sqlBuilder.toString());
            statement = transacaoDB.createStatement(sqlBuilder.toString());
            resultSet = statement.executeQuery(sqlBuilder.toString());

            if (resultSet != null && resultSet.next()) {
                objeto = mapearObjeto(resultSet);
            } else {
                throw new DatabaseException(new Exception(), "Não existe este registro!" + sqlBuilder.toString());
            }
        } catch (Exception e) {
            throw new DatabaseException(e, "Incapaz de consultar no banco! " + e.getMessage());

        } finally {
            try {
                if (resultSet != null)
                    resultSet.close();
                if (statement != null)
                    statement.close();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error: ", e);
            }
        }
        return objeto;
    }

    /**
     * atualizar objeto T no banco de dados
     */
    public T atualizar(T objeto) throws DatabaseException {
        Statement statement = null;

        try {
            StringBuilder sqlBuilder = new StringBuilder();

            sqlBuilder.append("UPDATE ");
            sqlBuilder.append(getNomeTabela());
            sqlBuilder.append(" SET ");
            sqlBuilder.append(criarComandoUpdate(objeto));
            sqlBuilder.append(" WHERE ID = ");
            sqlBuilder.append(getIdValue(objeto));

            System.out.println(sqlBuilder.toString());
            statement = transacaoDB.createStatement(sqlBuilder.toString());
            statement.execute(sqlBuilder.toString());
        } catch (Exception e) {
            throw new DatabaseException(e, "Incapaz de atualizar no banco de dados" + e.getMessage());
        } finally {
            try {
                if (statement != null)
                    statement.close();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error: ", e);
            }
        }
        return null;
    }

    /**
     * deletar objeto T no banco de dados
     */
    public T deletar(Integer id) throws DatabaseException {
        Statement statement = null;

        try {
            StringBuilder sqlBuilder = new StringBuilder();

            sqlBuilder.append("DELETE FROM ");
            sqlBuilder.append(getNomeTabela());
            sqlBuilder.append(" WHERE ID = ");
            sqlBuilder.append(id);

            System.out.println(sqlBuilder.toString());
            statement = transacaoDB.createStatement(sqlBuilder.toString());
            statement.execute(sqlBuilder.toString());
        } catch (Exception e) {
            throw new DatabaseException(e, "Incapaz de deletar no banco de dados" + e.getMessage());
        } finally {
            try {
                if (statement != null)
                    statement.close();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error: ", e);
            }
        }
        return null;
    }

    /**
     * listar objeto T no banco de dados
     */
    public List<T> listar() throws DatabaseException {
        List<T> list = new ArrayList<T>();
        Statement statement = null;
        ResultSet resultSet = null;
        T objetoT = null;

        try {
            StringBuilder sqlBuilder = new StringBuilder();

            sqlBuilder.append("SELECT * FROM ");
            sqlBuilder.append(getNomeTabela());
            sqlBuilder.append(" ORDER BY 1 ");

            System.out.println(sqlBuilder.toString());
            statement = transacaoDB.createStatement(sqlBuilder.toString());

            resultSet = statement.executeQuery(sqlBuilder.toString());

            while (resultSet != null && resultSet.next()) {
                objetoT = mapearObjeto(resultSet);
                list.add(objetoT);
            }
        } catch (Exception e) {
            throw new DatabaseException(e, "Incapaz de listar " + e.getMessage());
        } finally {
            try {
                if (resultSet != null)
                    resultSet.close();
                if (statement != null)
                    statement.close();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error: ", e);
            }
        }
        return list;
    }

    /**
     * Pegar o nome da tabela no banco de dados
     * 
     * @return nomeTabela = funcionario / relacao
     */
    private String getNomeTabela() {
        String result = oClass.getAnnotation(Tabela.class).nomeTabela();
        return result;
    }

    /**
     * cria instrução de "values" no "insert" com valores dos objetos
     * 
     * @param objeto
     * @return
     */
    private String criarComandoInsercao(T objeto) {
        StringBuilder sql = new StringBuilder();
        StringBuilder campos = new StringBuilder();
        StringBuilder valores = new StringBuilder();

        try {
            campos.append("(");
            valores.append("(");

            for (Field field : objeto.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Coluna coluna = field.getAnnotation(Coluna.class);

                if (!"id".equalsIgnoreCase(coluna.nomeColuna())) {
                    if (campos.length() > 1)
                        campos.append(", ");
                    if (valores.length() > 1)
                        valores.append(", ");
                }
                valores.append(coluna.nomeColuna());

                if (isUsingQuotes(field.getType())) {
                    valores.append("'");
                    valores.append(field.get(objeto));
                    valores.append("'");
                } else {
                    valores.append(field.get(objeto));
                }
            }
            campos.append(")");
            valores.append(")");

            sql.append(campos);
            sql.append("VALUES");
            sql.append(valores);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Não foi possível criar comando de inserção", e);
        }

        return sql.toString();
    }

    /**
     * cria instrução "set" no "update" com valores dos objetos
     * 
     * @param objeto
     * @return
     */
    private String criarComandoUpdate(T objeto) {
        StringBuilder sqlBuilder = new StringBuilder();

        try {
            for (Field field : objeto.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Coluna coluna = field.getAnnotation(Coluna.class);

                if (sqlBuilder.length() > 1) {
                    sqlBuilder.append(", ");
                }
                sqlBuilder.append((coluna.nomeColuna()));
                sqlBuilder.append(" = ");

                if (isUsingQuotes(field.getType())) {
                    sqlBuilder.append("'");
                    sqlBuilder.append(field.get(objeto));
                    sqlBuilder.append("'");
                } else {
                    sqlBuilder.append(field.get(objeto));
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Não foi possível criar comando de atualização ", e);
        }
        return sqlBuilder.toString();
    }

    /**
     * mapear campos do resultSet nas respectivas propriedades do objeto T
     * 
     * @param resultSet
     * @return
     */
    private T mapearObjeto(ResultSet resultSet) {
        T objeto = null;

        try {
            objeto = (T) oClass.newInstance();

            for (Field field : oClass.getDeclaredFields()) {
                field.setAccessible(true);

                Coluna coluna = field.getAnnotation(Coluna.class);
                Object valor = resultSet.getObject(coluna.nomeColuna());
                Class<?> tipo = field.getType();

                if (isPrimitive(tipo)) {
                    Class<?> boxed = mapPrimitiveClass(tipo);
                    valor = boxed.cast(valor);
                }
                field.set(objeto, valor);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Não foi possivel mapear o resultSet nas devidas propriedades do objeto ", e);
        }

        return objeto;
    }

    /**
     * obter o id do objeto
     * 
     * @param objeto
     * @return
     */
    private String getIdValue(T objeto) {
        StringBuilder sqlBuilder = new StringBuilder();

        try {
            Field field = objeto.getClass().getDeclaredField("id");
            field.setAccessible(true);
            sqlBuilder.append(field.get(objeto));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Não foi encontrado o id", e);
        }

        return sqlBuilder.toString();
    }

    /**
     * verificar se é primitivo
     * 
     * @param type
     * @return
     */
    private boolean isPrimitive(Class<?> type) {
        return (type == int.class || type == long.class || type == double.class || type == float.class
                || type == boolean.class || type == byte.class || type == char.class || type == short.class
                || type == Date.class);
    }

    /**
     * verificar tipo para identificar se precisa de aspas simples ou não
     * 
     * @param type
     * @return
     */
    private boolean isUsingQuotes(Class<?> type) {
        if (type == int.class || type == long.class || type == double.class || type == float.class || type == Date.class
                || type == Integer.class || type == Long.class || type == Double.class || type == Float.class) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Mapear classe primitiva
     * 
     * @param type
     * @return
     */
    private Class<?> mapPrimitiveClass(Class<?> type) {
        if (type == int.class) {
            return Integer.class;
        } else if (type == long.class) {
            return Long.class;
        } else if (type == double.class) {
            return Double.class;
        } else if (type == float.class) {
            return Float.class;
        } else if (type == boolean.class) {
            return Boolean.class;
        } else if (type == byte.class) {
            return Byte.class;
        } else if (type == char.class) {
            return Character.class;
        } else if (type == short.class) {
            return Short.class;
        } else if (type == Date.class) {
            return Date.class;
        } else {
            String string = "Class '" + type.getName() + "' is not a primitive";
            throw new IllegalArgumentException(string);
        }
    }

}
