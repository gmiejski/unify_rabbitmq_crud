package org.miejski.questions.events;

import org.miejski.exceptions.IdNotMatchingException;
import org.miejski.questions.QuestionID;
import org.miejski.questions.QuestionState;
import org.miejski.questions.QuestionStateChecker;

import java.time.ZonedDateTime;

public class QuestionDeleted implements QuestionModifier {

    private int questionID;
    private String market;
    private ZonedDateTime deleteDate;

    public QuestionDeleted(String market, int questionID, ZonedDateTime deleteDate) {
        this.questionID = questionID;
        this.market = market;
        this.deleteDate = deleteDate;
    }

    public QuestionDeleted() {
    }

    @Override
    public String ID() {
        return QuestionID.from(this.market, this.questionID);
    }

    @Override
    public QuestionState doSomething(QuestionState obj) {
        if (QuestionStateChecker.idNotMatching(obj, this.ID())) {
            throw new IdNotMatchingException("Wrong id");
        }
        if (!QuestionStateChecker.isInitialized(obj)) {
            return new QuestionState(market, questionID, "").delete(this.deleteDate);
        }
        return obj
                .withLastModification(obj.getLastModification())
                .delete(this.deleteDate);
    }

    public int getQuestionID() {
        return questionID;
    }

    public String getMarket() {
        return market;
    }

    public ZonedDateTime getDeleteDate() {
        return deleteDate;
    }
}
