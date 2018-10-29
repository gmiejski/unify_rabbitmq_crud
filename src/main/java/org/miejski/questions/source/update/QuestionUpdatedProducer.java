package org.miejski.questions.source.update;

import org.apache.commons.lang3.RandomStringUtils;
import org.miejski.questions.source.SourceEventProducer;

import java.time.ZonedDateTime;
import java.util.function.Supplier;

public class QuestionUpdatedProducer implements SourceEventProducer<QuestionUpdated> {

    private String market;
    private Supplier<Integer> questionIDSupplier;

    public QuestionUpdatedProducer(String market, Supplier<Integer> questionIDSupplier) {
        this.market = market;
        this.questionIDSupplier = questionIDSupplier;
    }

    public QuestionUpdated create() {
        return new QuestionUpdated(market, new QuestionUpdatedPayload(questionIDSupplier.get(), this.generateContent(), ZonedDateTime.now()));
    }

    private String generateContent() {
        return RandomStringUtils.randomAscii(10);
    }
}
