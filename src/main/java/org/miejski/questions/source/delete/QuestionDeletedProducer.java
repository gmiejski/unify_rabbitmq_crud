package org.miejski.questions.source.delete;

import org.apache.commons.lang3.RandomStringUtils;
import org.miejski.questions.source.SourceEventProducer;

import java.time.ZonedDateTime;
import java.util.function.Supplier;

public class QuestionDeletedProducer implements SourceEventProducer<QuestionDeleted> {

    private String market;
    private Supplier<Integer> questionIDSupplier;

    public QuestionDeletedProducer(String market, Supplier<Integer> questionIDSupplier) {
        this.market = market;
        this.questionIDSupplier = questionIDSupplier;
    }

    public QuestionDeleted create() {
        return new QuestionDeleted(market, new QuestionDeletedPayload(questionIDSupplier.get(), this.generateContent(), ZonedDateTime.now()));
    }

    private String generateContent() {
        return RandomStringUtils.randomAscii(10);
    }
}
