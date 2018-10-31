package org.miejski.questions.source.delete;

import org.miejski.exceptions.IdNotMatchingException;
import org.miejski.questions.events.QuestionModifier;
import org.miejski.questions.state.QuestionState;
import org.miejski.questions.state.QuestionStateChecker;

public class SourceQuestionDeleted implements QuestionModifier {

    private  String market;
    private  SourceQuestionDeletedPayload payload;

    public SourceQuestionDeleted() {
    }

    public SourceQuestionDeleted(String market, SourceQuestionDeletedPayload payload) {
        this.market = market;
        this.payload = payload;
    }

    public String getMarket() {
        return market;
    }

    public SourceQuestionDeletedPayload getPayload() {
        return payload;
    }


    @Override
    public QuestionState doSomething(QuestionState obj) {
        if (QuestionStateChecker.idNotMatching(obj, this.ID())) {
            throw new IdNotMatchingException("Wrong id");
        }
        if (!QuestionStateChecker.isInitialized(obj)) {
            return new QuestionState(market, payload.getQuestionID(), "").delete(this.payload.getDeletedAt());
        }
        return obj
                .withLastModification(obj.getLastModification())
                .delete(this.payload.getDeletedAt());
    }

    @Override
    public String ID() {
        return String.valueOf(payload.getQuestionID());
    }

}
