package org.miejski.questions.source.create;

public class QuestionCreated {

    private final String market;
    private final QuestionCreatedPayload payload;

    public QuestionCreated(String market, QuestionCreatedPayload payload) {
        this.market = market;
        this.payload = payload;
    }

    public String getMarket() {
        return market;
    }

    public QuestionCreatedPayload getPayload() {
        return payload;
    }
}
