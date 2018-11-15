package org.miejski.questions.source;

import java.util.Random;
import java.util.function.Supplier;

public class RandomQuestionIDProvider implements Supplier<Integer> {

    private final int maxQuestionID;
    private final Random random;

    public RandomQuestionIDProvider(int maxQuestionID) {
        this.maxQuestionID = maxQuestionID;
        this.random = new Random();
    }

    @Override
    public Integer get() {
        return this.random.nextInt(maxQuestionID);
    }
}
