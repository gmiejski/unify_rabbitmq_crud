package org.miejski.questions;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.miejski.questions.state.QuestionState;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.ZonedDateTime;

@Entity
public class QuestionStateEntity {

    @Id
    public Integer id = null;
    @Nullable
    public String content;
    @Nullable
    public String market;
    @Nullable
    public ZonedDateTime deleteDate;
    @Nullable
    public ZonedDateTime lastModification;

    @NotNull
    public QuestionState toQuestionState() {
        return new QuestionState(market, id, content).withLastModification(lastModification).delete(deleteDate);
    }
}