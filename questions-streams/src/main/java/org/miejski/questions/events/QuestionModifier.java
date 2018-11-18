package org.miejski.questions.events;

import org.miejski.Modifier;
import org.miejski.questions.state.QuestionState;

public interface QuestionModifier extends Modifier<QuestionState> {
    String ID();

    QuestionState doSomething(QuestionState questionState);
}
