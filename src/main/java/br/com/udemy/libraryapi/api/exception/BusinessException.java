package br.com.udemy.libraryapi.api.exception;

public class BusinessException extends RuntimeException {

    public BusinessException(String s) {
        super(s);
    }
}
