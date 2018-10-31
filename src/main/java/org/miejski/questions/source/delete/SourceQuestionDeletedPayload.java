package org.miejski.questions.source.delete;

import java.time.ZonedDateTime;

public class SourceQuestionDeletedPayload {

    private  int questionID;
    private  String content;
    private  ZonedDateTime deletedAt;

    public SourceQuestionDeletedPayload() {
    }

    public SourceQuestionDeletedPayload(int questionID, String content, ZonedDateTime deletedAt) {
        this.questionID = questionID;
        this.content = content;
        this.deletedAt = deletedAt;
    }

    public int getQuestionID() {
        return questionID;
    }

    public String getContent() {
        return content;
    }

    public ZonedDateTime getDeletedAt() {
        return deletedAt;
    }
}
