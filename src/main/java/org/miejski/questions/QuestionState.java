package org.miejski.questions;

import com.google.common.base.Objects;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class QuestionState {

    private int questionID;
    private String market;
    private String content;
    private ZonedDateTime lastModification = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());

    public QuestionState(String market, int questionID, String content) {
        this.questionID = questionID;
        this.market = market;
        this.content = content;
    }

    public QuestionState() {
    }

    public String id() {
        return QuestionID.from(this.market, this.questionID);
    }

    public QuestionState withLastModification(ZonedDateTime lastModification) {
        QuestionState questionState = new QuestionState(this.market, this.questionID, this.content);
        questionState.lastModification = lastModification;
        return questionState;
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

    public ZonedDateTime getLastModification() {
        return lastModification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionState that = (QuestionState) o;
        return getQuestionID() == that.getQuestionID() &&
                Objects.equal(getMarket(), that.getMarket()) &&
                Objects.equal(getContent(), that.getContent()) &&
                getLastModification().isEqual(that.getLastModification());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getQuestionID(), getMarket(), getContent(), getLastModification());
    }
}
