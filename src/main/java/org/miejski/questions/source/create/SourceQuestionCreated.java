package org.miejski.questions.source.create;

import org.miejski.exceptions.IdNotMatchingException;
import org.miejski.questions.QuestionID;
import org.miejski.questions.events.QuestionModifier;
import org.miejski.questions.state.QuestionState;
import org.miejski.questions.state.QuestionStateChecker;

public class SourceQuestionCreated implements QuestionModifier {

    private String market;
    private SourceQuestionCreatedPayload payload;


    public SourceQuestionCreated() {
    }

    public SourceQuestionCreated(String market, SourceQuestionCreatedPayload payload) {
        this.market = market;
        this.payload = payload;
    }

    public String getMarket() {
        return market;
    }

    public SourceQuestionCreatedPayload getPayload() {
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
        return QuestionID.from(market, payload.getQuestionID());
    }
}
