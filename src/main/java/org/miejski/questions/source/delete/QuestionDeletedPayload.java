package org.miejski.questions.source.delete;

import java.time.ZonedDateTime;

public class QuestionDeletedPayload {

    private final int questionID;
    private final String content;
    private final ZonedDateTime deletedAt;

    public QuestionDeletedPayload(int questionID, String content, ZonedDateTime deletedAt) {
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
