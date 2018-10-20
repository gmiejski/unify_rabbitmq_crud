package org.miejski.simple.objects;

public class IdNotMatchingException extends RuntimeException {
    public IdNotMatchingException(String ID) {
        super(ID);
    }
}
