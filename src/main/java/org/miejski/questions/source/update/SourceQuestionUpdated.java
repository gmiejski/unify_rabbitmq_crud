package org.miejski.questions.source.update;

import org.miejski.Modifier;
import org.miejski.exceptions.IdNotMatchingException;
import org.miejski.questions.events.QuestionModifier;
import org.miejski.questions.state.QuestionState;
import org.miejski.questions.state.QuestionStateChecker;

public class SourceQuestionUpdated implements QuestionModifier {

    private String market;
    private SourceQuestionUpdatedPayload payload;

    public SourceQuestionUpdated() {
    }

    public SourceQuestionUpdated(String market, SourceQuestionUpdatedPayload payload) {
        this.market = market;
        this.payload = payload;
    }

    public String getMarket() {
        return market;
    }

    public SourceQuestionUpdatedPayload getPayload() {
        return payload;
    }

    @Override
    public QuestionState doSomething(QuestionState state) {
        if (QuestionStateChecker.idNotMatching(state, this.ID())) {
            throw new IdNotMatchingException("Wrong id");
        }
        if (!QuestionStateChecker.isInitialized(state)) {
            return new QuestionState(market, payload.getQuestionID(), payload.getContent()).withLastModification(this.payload.getEditedAt());
        }
        if (this.payload.getEditedAt().isBefore(state.getLastModification())) {
            return state;
        }
        return new QuestionState(market, payload.getQuestionID(), payload.getContent()).withLastModification(this.payload.getEditedAt()).delete(state.getDeleteDate());

    }

    @Override
    public String ID() {
        return String.valueOf(payload.getQuestionID());
    }

}
