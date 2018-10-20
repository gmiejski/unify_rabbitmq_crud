package org.miejski.questions;

import java.time.ZonedDateTime;

public class QuestionState {

    private int questionID;
    private String market;
    private String content;


    public QuestionState(int questionID, String market, String content) {
        this.questionID = questionID;
        this.market = market;
        this.content = content;
    }

    public QuestionState() {
    }

    public String id() {
        return QuestionID.from(this.market, this.questionID);
    }

    public QuestionState withLastModification(ZonedDateTime createDate) {
        return null;
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
}
