package org.miejski.questions.source.create;

import org.miejski.Modifier;
import org.miejski.exceptions.IdNotMatchingException;
import org.miejski.questions.state.QuestionState;
import org.miejski.questions.state.QuestionStateChecker;

public class QuestionCreated implements Modifier<QuestionState> {

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

    @Override
    public QuestionState doSomething(QuestionState obj) {
        if (QuestionStateChecker.idNotMatching(obj, this.ID())) {
            throw new IdNotMatchingException("Wrong id");
        }
        if (QuestionStateChecker.isInitialized(obj)) {
            return obj;
        }

        return new QuestionState(market, payload.getQuestionID(), payload.getContent()).withLastModification(this.payload.getCreateDate());
    }

    @Override
    public String ID() {
        return String.valueOf(payload.getQuestionID());
    }
}
