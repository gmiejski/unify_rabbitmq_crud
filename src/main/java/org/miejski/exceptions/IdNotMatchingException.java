package org.miejski.exceptions;

public class IdNotMatchingException extends RuntimeException {
    public IdNotMatchingException(String ID) {
        super(ID);
    }
}
