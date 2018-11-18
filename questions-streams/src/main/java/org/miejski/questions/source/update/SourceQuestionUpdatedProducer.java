package org.miejski.questions.source.update;

import org.apache.commons.lang3.RandomStringUtils;
import org.miejski.questions.source.SourceEventProducer;

import java.time.ZonedDateTime;
import java.util.function.Supplier;

public class SourceQuestionUpdatedProducer implements SourceEventProducer<SourceQuestionUpdated> {

    private String market;
    private Supplier<Integer> questionIDSupplier;

    public SourceQuestionUpdatedProducer(String market, Supplier<Integer> questionIDSupplier) {
        this.market = market;
        this.questionIDSupplier = questionIDSupplier;
    }

    public SourceQuestionUpdated create() {
        return new SourceQuestionUpdated(market, new SourceQuestionUpdatedPayload(questionIDSupplier.get(), this.generateContent(), ZonedDateTime.now()));
    }

    private String generateContent() {
        return RandomStringUtils.randomAscii(10);
    }
}
