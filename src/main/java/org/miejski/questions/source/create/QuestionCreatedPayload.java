package org.miejski.questions.source.create;

import java.time.ZonedDateTime;

public class QuestionCreatedPayload {

    private final int questionID;
    private final String content;
    private final ZonedDateTime createDate;

    public QuestionCreatedPayload(int questionID, String content, ZonedDateTime createDate) {
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
