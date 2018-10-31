package org.miejski.questions.source.update;

import java.time.ZonedDateTime;

public class SourceQuestionUpdatedPayload {

    private int questionID;
    private String content;
    private ZonedDateTime editedAt;

    public SourceQuestionUpdatedPayload() {
    }

    public SourceQuestionUpdatedPayload(int questionID, String content, ZonedDateTime editedAt) {
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
