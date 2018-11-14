package org.miejski.questions.source.delete;

import java.time.ZonedDateTime;

public class SourceQuestionDeletedPayload {

    private  int questionID;
    private  ZonedDateTime deletedAt;

    public SourceQuestionDeletedPayload() {
    }

    public SourceQuestionDeletedPayload(int questionID, ZonedDateTime deletedAt) {
        this.questionID = questionID;
        this.deletedAt = deletedAt;
    }

    public int getQuestionID() {
        return questionID;
    }

    public ZonedDateTime getDeletedAt() {
        return deletedAt;
    }
}
