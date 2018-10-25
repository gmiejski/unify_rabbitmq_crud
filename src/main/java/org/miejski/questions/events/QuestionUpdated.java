package org.miejski.questions.events;

import org.miejski.exceptions.IdNotMatchingException;
import org.miejski.questions.QuestionID;
import org.miejski.questions.state.QuestionState;
import org.miejski.questions.state.QuestionStateChecker;

import java.time.ZonedDateTime;

public class QuestionUpdated implements QuestionModifier {

    private String market;
    private int questionID;
    private String content;
    private ZonedDateTime updateDate;

    public QuestionUpdated(String market, int questionID, String content, ZonedDateTime updateDate) {
        this.market = market;
        this.questionID = questionID;
        this.content = content;
        this.updateDate = updateDate;
    }

    public QuestionUpdated() {
    }

    @Override
    public QuestionState doSomething(QuestionState state) {
        if (QuestionStateChecker.idNotMatching(state, this.ID())) {
            throw new IdNotMatchingException("Wrong id");
        }
        if (!QuestionStateChecker.isInitialized(state)) {
            return new QuestionState(market, questionID, content).withLastModification(this.updateDate);
        }
        if (this.updateDate.isBefore(state.getLastModification())) {
            return state;
        }
        return new QuestionState(market, questionID, content).withLastModification(this.updateDate).delete(state.getDeleteDate());

    }

    @Override
    public String ID() {
        return QuestionID.from(this.market, this.questionID);
    }


}
