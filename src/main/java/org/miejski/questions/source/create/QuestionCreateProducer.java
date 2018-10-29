package org.miejski.questions.source.create;

import org.apache.commons.lang3.RandomStringUtils;
import org.miejski.questions.source.SourceEventProducer;

import java.time.ZonedDateTime;
import java.util.function.Supplier;

public class QuestionCreateProducer implements SourceEventProducer<QuestionCreated> {

    private String market;
    private Supplier<Integer> questionIDSupplier;

    public QuestionCreateProducer(String market, Supplier<Integer> questionIDSupplier) {
        this.market = market;
        this.questionIDSupplier = questionIDSupplier;
    }

    public QuestionCreated create() {
        return new QuestionCreated(market, new QuestionCreatedPayload(questionIDSupplier.get(), this.generateContent(), ZonedDateTime.now()));
    }

    private String generateContent() {
        return RandomStringUtils.randomAscii(10);
    }
}
