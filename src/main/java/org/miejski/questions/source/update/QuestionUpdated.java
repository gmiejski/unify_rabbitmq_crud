package org.miejski.questions.source.update;

public class QuestionUpdated {

    private final String market;
    private final QuestionUpdatedPayload payload;

    public QuestionUpdated(String market, QuestionUpdatedPayload payload) {
        this.market = market;
        this.payload = payload;
    }

    public String getMarket() {
        return market;
    }

    public QuestionUpdatedPayload getPayload() {
        return payload;
    }
}
