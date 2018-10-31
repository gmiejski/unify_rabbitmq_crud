package org.miejski.questions.source.create;

import java.time.ZonedDateTime;

public class SourceQuestionCreatedPayload {

    private int questionID;
    private String content;
    private ZonedDateTime createDate;

    public SourceQuestionCreatedPayload() {
    }

    public SourceQuestionCreatedPayload(int questionID, String content, ZonedDateTime createDate) {
        this.questionID = questionID;
        this.content = content;
        this.createDate = createDate;
    }

    public int getQuestionID() {
        return questionID;
    }

    public String getContent() {
        return content;
    }

    public ZonedDateTime getCreateDate() {
        return createDate;
    }
}
