package org.miejski.questions.source.delete;

import org.apache.commons.lang3.RandomStringUtils;
import org.miejski.questions.source.SourceEventProducer;

import java.time.ZonedDateTime;
import java.util.function.Supplier;

public class SourceQuestionDeletedProducer implements SourceEventProducer<SourceQuestionDeleted> {

    private String market;
    private Supplier<Integer> questionIDSupplier;

    public SourceQuestionDeletedProducer(String market, Supplier<Integer> questionIDSupplier) {
        this.market = market;
        this.questionIDSupplier = questionIDSupplier;
    }

    public SourceQuestionDeleted create() {
        return new SourceQuestionDeleted(market, new SourceQuestionDeletedPayload(questionIDSupplier.get(), this.generateContent(), ZonedDateTime.now()));
    }

    private String generateContent() {
        return RandomStringUtils.randomAscii(10);
    }
}
