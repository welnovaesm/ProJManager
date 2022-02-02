package br.com.proj.database;

import java.io.Serializable;
import java.util.List;

import br.com.proj.util.DatabaseException;

public interface GenericDAO<T, ID extends Serializable> {

    /**
     * retornar classe para persistir
     * 
     * @return
     */
    public Class<T> getObjectClass();

    /**
     * pega transação se ja for existente
     * 
     * @return
     */
    public TransacaoDB getTransacaoDB();

    /**
     * criar transacao que está ocorrendo
     * 
     * @param transacaoDB
     */
    public void setTransacaoDB(TransacaoDB transacaoDB);

    /**
     * criar objetos "T" no banco de dados
     * 
     * @param objeto
     * @return
     * @throws DatabaseException
     */
    public T criar(T objeto) throws DatabaseException;

    /**
     * consultar objetos "T" no banco de dados
     * 
     * @param id
     * @return
     * @throws DatabaseException
     */
    public T consultar(Integer id) throws DatabaseException;

    /**
     * atualizar objetos "T" no banco de dados
     * 
     * @param objeto
     * @return
     * @throws DatabaseException
     */
    public T atualizar(T objeto) throws DatabaseException;

    /**
     * deletar objetos "T" no banco de dados
     * 
     * @param id
     * @return
     * @throws DatabaseException
     */
    public T deletar(Integer id) throws DatabaseException;

    /**
     * listar objetos "T" no banco de dados
     * 
     * @return
     * @throws DatabaseException
     */
    public List<T> listar() throws DatabaseException;

}
