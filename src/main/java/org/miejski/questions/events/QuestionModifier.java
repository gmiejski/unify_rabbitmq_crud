package org.miejski.questions.events;

import org.miejski.questions.QuestionState;

public interface QuestionModifier {
    String ID();

    QuestionState modify(QuestionState questionState);
}
