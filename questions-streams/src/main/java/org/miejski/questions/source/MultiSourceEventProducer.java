package org.miejski.questions.source;

import org.apache.kafka.streams.KeyValue;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class MultiSourceEventProducer<T> implements SourceEventProducer<T> {

    SourceEventProducer<T> proxy;

    public MultiSourceEventProducer(SourceEventProducer<T> proxy) {
        this.proxy = proxy;
    }

    public List<T> create(int count) {
        return IntStream.range(0, count).boxed().map(x -> proxy.create()).collect(Collectors.toList());
    }

    public <P> List<T> create(int count, Function<T, P> identityFunction) {
        Stream<T> generatedEvents = IntStream.range(0, count).boxed().map(x -> proxy.create());
        List<T> createEvents = generatedEvents
                .map(event -> new KeyValue<>(identityFunction.apply(event), event))
                .collect(Collectors.groupingBy(kv -> kv.key))
                .entrySet().stream()
                .map(x -> x.getValue().get(0).value)
                .collect(toList());


        return IntStream.range(0, count).boxed()
                .map(x -> createEvents.get(x % createEvents.size()))
                .collect(toList());
    }

    @Override
    public T create() {
        return proxy.create();
    }
}
