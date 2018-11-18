package org.miejski.questions.source;

import org.apache.kafka.streams.KeyValue;
import org.miejski.questions.source.create.SourceQuestionCreated;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class MultiCreateSourceEventProducer extends MultiSourceEventProducer<SourceQuestionCreated> {

    public MultiCreateSourceEventProducer(SourceEventProducer<SourceQuestionCreated> proxy) {
        super(proxy);
    }

    public List<SourceQuestionCreated> create(int count) {
        List<SourceQuestionCreated> generatedEvents = IntStream.range(0, count).boxed().map(x -> proxy.create()).collect(toList());
        List<KeyValue<String, SourceQuestionCreated>> keyValueStream = generatedEvents.stream()
                .map(event -> new KeyValue<>(event.ID(), event)).collect(toList());
        Map<String, List<KeyValue<String, SourceQuestionCreated>>> collect = keyValueStream.stream()
                .collect(Collectors.groupingBy(kv -> kv.key));
        List<SourceQuestionCreated> createEvents = collect
                .entrySet().stream()
                .map(x -> x.getValue().get(0).value)
                .collect(toList());

        return IntStream.range(0, count).boxed()
                .map(x -> createEvents.get(x % createEvents.size()))
                .collect(toList());
    }
}
