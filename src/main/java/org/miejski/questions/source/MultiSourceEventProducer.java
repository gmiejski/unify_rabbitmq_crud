package org.miejski.questions.source;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MultiSourceEventProducer<T> implements SourceEventProducer<T> {

    private SourceEventProducer<T> proxy;

    public MultiSourceEventProducer(SourceEventProducer<T> proxy) {
        this.proxy = proxy;
    }

    public List<T> create(int count) {
        return IntStream.range(0, count).boxed().map(x -> proxy.create()).collect(Collectors.toList());
    }

    @Override
    public T create() {
        return proxy.create();
    }
}
