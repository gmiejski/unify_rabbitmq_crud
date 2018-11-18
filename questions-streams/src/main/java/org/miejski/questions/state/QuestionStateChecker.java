package org.miejski.questions.state;

import com.google.common.base.Strings;

public class QuestionStateChecker {
    public static boolean idNotMatching(QuestionState questionState, String id) {
        return false;
    }

    public static boolean isInitialized(QuestionState state) {
        return state != null && !Strings.isNullOrEmpty(state.id());
    }

}
