package org.miejski.questions.state;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.miejski.questions.QuestionID;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class QuestionState {

    private int questionID;
    private String market;
    private String content;
    private ZonedDateTime lastModification = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
    private ZonedDateTime deletedDate = null;

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
        questionState.deletedDate = this.deletedDate;
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

        if (this.id() == null && !this.id().equals(that.id())) {
            return false;
        }

        if (this.isDeleted() && that.isDeleted()) {
            return true;
            // TODO Would be great to have this, but this would fail commutative delete before create :(
            // return this.getValue() == that.getValue();
        }
        if (this.isDeleted() || that.isDeleted()) {
            return false;
        }

        return Objects.equal(getContent(), that.getContent()) &&
                getLastModification().isEqual(that.getLastModification());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getQuestionID(), getMarket(), getContent(), getLastModification());
    }

    @JsonIgnore
    public boolean isDeleted() {
        return this.deletedDate != null;
    }

    public QuestionState delete(ZonedDateTime deleteDate) {
        QuestionState questionState = new QuestionState(this.market, this.questionID, this.content);
        questionState = questionState.withLastModification(this.lastModification);
        questionState.deletedDate = deleteDate;
        return questionState;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("questionID", questionID)
                .add("market", market)
                .add("content", content)
                .add("lastModification", lastModification)
                .add("deletedDate", deletedDate)
                .toString();
    }

    @JsonIgnore
    public ZonedDateTime getDeleteDate() {
        return this.deletedDate;
    }
}
