package org.miejski.questions.source.delete;

import java.time.ZonedDateTime;

public class QuestionDeletedPayload {

    private final int questionID;
    private final String content;
    private final ZonedDateTime editedAt;

    public QuestionDeletedPayload(int questionID, String content, ZonedDateTime editedAt) {
        this.questionID = questionID;
        this.content = content;
        this.editedAt = editedAt;
    }

    public int getQuestionID() {
        return questionID;
    }

    public String getContent() {
        return content;
    }

    public ZonedDateTime getEditedAt() {
        return editedAt;
    }
}
