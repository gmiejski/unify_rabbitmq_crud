package org.miejski.questions;


import com.google.common.base.Strings;

public class QuestionID {

    public static String from(String market, int questionID) {
        if (Strings.isNullOrEmpty(market) || questionID < 0) {
            return null;
        }
        return market + "-" + String.valueOf(questionID);
    }
}
