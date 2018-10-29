package org.miejski.questions.source.delete;

public class QuestionDeleted {

    private final String market;
    private final QuestionDeletedPayload payload;

    public QuestionDeleted(String market, QuestionDeletedPayload payload) {
        this.market = market;
        this.payload = payload;
    }

    public String getMarket() {
        return market;
    }

    public QuestionDeletedPayload getPayload() {
        return payload;
    }
}
