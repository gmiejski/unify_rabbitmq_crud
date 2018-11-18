package org.miejski;

public interface Modifier<T> {
    T doSomething(T questionState);
    String ID();
}
