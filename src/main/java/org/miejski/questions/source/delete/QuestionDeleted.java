package org.miejski.questions.source.delete;

import org.miejski.Modifier;
import org.miejski.exceptions.IdNotMatchingException;
import org.miejski.questions.state.QuestionState;
import org.miejski.questions.state.QuestionStateChecker;

public class QuestionDeleted implements Modifier<QuestionState> {

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
