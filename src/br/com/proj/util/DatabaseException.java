package br.com.proj.util;

public class DatabaseException extends Exception {
    
    private static final long serialVersionUID = -7550380487682283316L;
    private final Exception ex;
    private final String msg;

    public DatabaseException(Exception e) {
        ex = e;
        msg = e.getMessage();
    }

    public DatabaseException(Exception exception, String mensagem){
        this.ex = exception;
        this.msg = mensagem;
    }

    public Exception getEx(){
        return ex;
    }

    public String getMsg(){
        return msg;
    }

}
