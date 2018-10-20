package org.miejski.simple.objects.consistecy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.miejski.simple.objects.events.ObjectCreation;
import org.miejski.simple.objects.events.ObjectUpdate;

import java.time.ZonedDateTime;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

class GeneratedDataTest {

    private int maxID = 5;


    @Test
    void allCreateEventsHaveSameDateTime() {
        EventsPercentages eventsPercentages = new EventsPercentages(50, 30, 20);
        GeneratedData generatedData = new Generator(eventsPercentages, 100, maxID).generate();

        for (int id = 0; id < maxID; id++) {
            Set<ZonedDateTime> allDateTimes = generatedData.getEventsFor(String.valueOf(id))
                    .stream()
                    .filter(x -> x instanceof ObjectCreation)
                    .map(x -> ((ObjectCreation) x).getCreateDate())
                    .collect(toSet());

            Assertions.assertEquals(1, allDateTimes.size());
        }
    }

    @Test
    void allCreateEventsHaveSameValue() {
        EventsPercentages eventsPercentages = new EventsPercentages(50, 30, 20);
        GeneratedData generatedData = new Generator(eventsPercentages, 100, maxID).generate();

        for (int id = 0; id < maxID; id++) {
            Set<Integer> values = generatedData.getEventsFor(String.valueOf(id))
                    .stream()
                    .filter(x -> x instanceof ObjectCreation)
                    .map(x -> ((ObjectCreation) x).getValue())
                    .collect(toSet());
            Assertions.assertEquals(1, values.size());
        }
    }

    @Test
    void allUpdatesHaveEventDateAfterCreateEvent() {
        EventsPercentages eventsPercentages = new EventsPercentages(50, 30, 20);
        GeneratedData generatedData = new Generator(eventsPercentages, 100, maxID).generate();

        for (int id = 0; id < maxID; id++) {
            ObjectCreation objectCreation = generatedData.getEventsFor(String.valueOf(id))
                    .stream()
                    .filter(x -> x instanceof ObjectCreation)
                    .map(x -> (ObjectCreation) x)
                    .findFirst().orElseThrow(() -> new RuntimeException("Error"));

            Set<ZonedDateTime> allUpdatesDates = generatedData.getEventsFor(String.valueOf(id))
                    .stream()
                    .filter(x -> x instanceof ObjectUpdate)
                    .map(x -> ((ObjectUpdate) x).getUpdateDate())
                    .collect(toSet());

            allUpdatesDates.forEach((updateDate) -> Assertions.assertTrue(objectCreation.getCreateDate().isBefore(updateDate)));
        }
    }
}