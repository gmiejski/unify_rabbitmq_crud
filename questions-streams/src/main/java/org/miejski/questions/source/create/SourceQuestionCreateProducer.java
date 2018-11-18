package org.miejski.questions.source.create;

import org.apache.commons.lang3.RandomStringUtils;
import org.miejski.questions.source.SourceEventProducer;

import java.time.ZonedDateTime;
import java.util.function.Supplier;

public class SourceQuestionCreateProducer implements SourceEventProducer<SourceQuestionCreated> {

    private String market;
    private Supplier<Integer> questionIDSupplier;

    public SourceQuestionCreateProducer(String market, Supplier<Integer> questionIDSupplier) {
        this.market = market;
        this.questionIDSupplier = questionIDSupplier;
    }

    public SourceQuestionCreated create() {
        return new SourceQuestionCreated(market, new SourceQuestionCreatedPayload(questionIDSupplier.get(), this.generateContent(), ZonedDateTime.now()));
    }

    private String generateContent() {
        return RandomStringUtils.randomAscii(10);
    }
}
