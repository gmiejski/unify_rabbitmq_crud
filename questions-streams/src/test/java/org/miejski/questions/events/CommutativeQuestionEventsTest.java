package org.miejski.questions.events;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.miejski.questions.state.QuestionState;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

class CommutativeQuestionEventsTest {

    private static final String market = "us";
    private static final int questionID = 1;
    private static final ZonedDateTime startingDateTime = ZonedDateTime.of(2018, 10, 10, 0, 0, 0, 0, ZoneId.systemDefault());
    private static final String initialContent = "createdContent";
    private static final String updatedContent = "updatedContent";
    private static final String secondUpdateContent = "changed";

    private QuestionCreated QuestionCreated = new QuestionCreated(market, questionID, initialContent, startingDateTime);
    private QuestionUpdated QuestionUpdated = new QuestionUpdated(market, questionID, updatedContent, startingDateTime.plusMinutes(1));
    private QuestionDeleted QuestionDeleted = new QuestionDeleted(market, questionID, startingDateTime.plusMinutes(2));

    @Test
    void CreateAndUpdate() {
        QuestionState state1 = apply(QuestionCreated, QuestionUpdated);
        QuestionState state2 = apply(QuestionUpdated, QuestionCreated);

        Assertions.assertEquals(state1, state2);
    }

    @Test
    void CreateAndDelete() {
        QuestionState state1 = apply(QuestionCreated, QuestionDeleted);
        QuestionState state2 = apply(QuestionDeleted, QuestionCreated);

        Assertions.assertEquals(state1, state2);
    }

    @Test
    void UpdateAndDelete() {
        QuestionState state1 = apply(QuestionDeleted, QuestionUpdated);
        QuestionState state2 = apply(QuestionUpdated, QuestionDeleted);

        Assertions.assertEquals(state1, state2);
    }

    @Test
    void DoubleCreate() {
        QuestionState state1 = apply(QuestionCreated, QuestionUpdated, QuestionCreated);
        QuestionState state2 = apply(QuestionCreated, QuestionUpdated);

        Assertions.assertEquals(state1, state2);
    }

    @Test
    void updateFromPastDoesNotUpdateValue() {
        QuestionState state = apply(QuestionCreated, QuestionUpdated);
        QuestionModifier secondUpdate = new QuestionUpdated(market, questionID, secondUpdateContent, startingDateTime.minusMinutes(1));

        state = secondUpdate.doSomething(state);

        Assertions.assertEquals(updatedContent, state.getContent());
        Assertions.assertEquals(startingDateTime.plusMinutes(1), state.getLastModification());
        Assertions.assertEquals(QuestionUpdated.ID(), state.id());
    }

    @Test
    void updatesAfterDeletionCanStillUpdateContent() {
        QuestionState state = apply(QuestionCreated, QuestionUpdated, QuestionDeleted);
        QuestionModifier secondUpdate = new QuestionUpdated(market, questionID, secondUpdateContent, startingDateTime.plusHours(1));

        state = secondUpdate.doSomething(state);

        Assertions.assertEquals(secondUpdateContent, state.getContent());
        Assertions.assertTrue(state.isDeleted());
    }

    @Test
    void testSeveralUpdates() {
        QuestionState state = apply(QuestionCreated, QuestionUpdated);
        QuestionModifier secondUpdate = new QuestionUpdated(market, questionID, secondUpdateContent, startingDateTime.plusHours(1));

        state = secondUpdate.doSomething(state);

        Assertions.assertEquals(secondUpdateContent, state.getContent());
        Assertions.assertFalse(state.isDeleted());
    }

    @Test
    void applyingEventFromPastDoesNotChangeLastModificationTime() {
        QuestionState state = apply(QuestionCreated);
        ZonedDateTime initialDate = state.getLastModification();
        List<QuestionModifier> events = Arrays.asList(
                new QuestionCreated(market, questionID, initialContent, startingDateTime.minusDays(1)),
                new QuestionUpdated(market, questionID, updatedContent, startingDateTime.minusDays(2)),
                new QuestionDeleted(market, questionID, startingDateTime.minusDays(3)));

        for (QuestionModifier modifier : events) {
            state = modifier.doSomething(state);
            Assertions.assertTrue(initialDate.isEqual(state.getLastModification()));
        }
    }

    @Test
    void AllThreeCommutative() {
        List<QuestionState> all = Arrays.asList(
                apply(QuestionCreated, QuestionUpdated, QuestionDeleted),
                apply(QuestionCreated, QuestionDeleted, QuestionUpdated),
                apply(QuestionDeleted, QuestionUpdated, QuestionCreated),
                apply(QuestionDeleted, QuestionCreated, QuestionUpdated),
                apply(QuestionUpdated, QuestionDeleted, QuestionCreated),
                apply(QuestionUpdated, QuestionCreated, QuestionDeleted));

        for (int i = 0; i < all.size() - 1; i++) {
            for (int j = i + 1; j < all.size(); j++) {
                Assertions.assertEquals(all.get(i), all.get(j));
            }
        }
    }

    private QuestionState apply(QuestionModifier... events) {
        QuestionState result = new QuestionState();
        for (QuestionModifier event : events) {
            result = event.doSomething(result);
        }
        return result;
    }
}