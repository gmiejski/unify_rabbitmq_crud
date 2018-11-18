package org.miejski.questions;


import com.google.common.base.Strings;

public class QuestionID {

    private String market;
    private int questionID;

    public QuestionID(String market, int questionID) {
        this.market = market;
        this.questionID = questionID;
    }

    public String getMarket() {
        return market;
    }

    public int getQuestionID() {
        return questionID;
    }

    public static QuestionID from(String marketWithID) {
        String[] split = marketWithID.split("-");
        return new QuestionID(split[0], Integer.valueOf(split[1]));
    }

    public static String from(String market, int questionID) {
        if (Strings.isNullOrEmpty(market) || questionID < 0) {
            return null;
        }
        return market + "-" + String.valueOf(questionID);
    }

    public static String from(String market, String questionID) {
        Integer intQuestionID;
        try {
            intQuestionID = Integer.valueOf(questionID);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }

        if (Strings.isNullOrEmpty(market) || intQuestionID < 0) {
            return null;
        }
        return market + "-" + String.valueOf(questionID);
    }

}
