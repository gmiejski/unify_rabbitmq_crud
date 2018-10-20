package org.miejski.questions.events;

import org.miejski.questions.QuestionID;
import org.miejski.questions.QuestionState;
import org.miejski.exceptions.IdNotMatchingException;
import org.miejski.questions.QuestionStateChecker;

import java.time.ZonedDateTime;

public class QuestionCreated implements QuestionModifier {

    private int questionID;
    private String market;
    private String content;
    private ZonedDateTime createDate;

    public QuestionCreated(String market, int questionID, String content, ZonedDateTime createDate) {
        this.questionID = questionID;
        this.market = market;
        this.content = content;
        this.createDate = createDate;
    }

    public QuestionCreated() {
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
        if (QuestionStateChecker.isInitialized(obj)) {
            return obj;
        }
        return new QuestionState(market, questionID, content).withLastModification(this.createDate);
    }

    public int getQuestionID() {
        return questionID;
    }

    public String getMarket() {
        return market;
    }

    public String getContent() {
        return content;
    }

    public ZonedDateTime getCreateDate() {
        return createDate;
    }
}
