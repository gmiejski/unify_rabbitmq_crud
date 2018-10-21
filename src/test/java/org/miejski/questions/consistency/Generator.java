package org.miejski.questions.consistency;

import org.apache.commons.lang3.RandomStringUtils;
import org.miejski.questions.QuestionState;
import org.miejski.questions.events.QuestionCreated;
import org.miejski.questions.events.QuestionDeleted;
import org.miejski.questions.events.QuestionModifier;
import org.miejski.questions.events.QuestionUpdated;
import org.miejski.simple.objects.consistecy.EventsPercentages;
import org.miejski.simple.objects.events.ObjectDelete;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

class GeneratedData {

    private List<QuestionModifier> events;

    public GeneratedData(List<QuestionModifier> collect) {
        this.events = collect;
    }

    public List<QuestionModifier> getEvents() {
        return this.events;
    }

    public Map<String, QuestionState> finalState() {
        Map<String, QuestionState> finalStates = this.events.stream()
                .collect(groupingBy(QuestionModifier::ID))
                .entrySet().stream()
                .collect(toMap(Map.Entry::getKey, kv -> this.toQuestionState(kv.getValue())));
        return finalStates;
    }

    private QuestionState toQuestionState(List<QuestionModifier> events) {
        QuestionState result = new QuestionState();
        for (QuestionModifier event : events) {
            result = event.doSomething(result);
        }
        return result;
    }

    public List<QuestionModifier> getEventsFor(String ID) {
        return this.events.stream().filter(event -> event.ID().equals(ID)).collect(toList());
    }
}

public class Generator {

    private final ZonedDateTime startingDateTime = ZonedDateTime.of(2018, 10, 10, 0, 0, 0, 0, ZoneId.systemDefault());
    private final EventsPercentages eventsPercentages;
    private final int eventsCount;
    private int maxObjectID;
    private String market;

    public Generator(EventsPercentages eventsPercentages, int eventsCount, int maxObjectID, String market) {
        this.eventsPercentages = eventsPercentages;
        this.eventsCount = eventsCount;
        this.maxObjectID = maxObjectID;
        this.market = market;
    }

    public GeneratedData generate() {

        Map<Integer, QuestionCreated> createEventsForAllStates = generateCreateEvents(this.maxObjectID);
        List<QuestionModifier> createEvents = createEventsForAllStates.values().stream().map(x -> (QuestionModifier) x).collect(toList());
        List<QuestionModifier> collect = IntStream.range(createEventsForAllStates.size(), eventsCount)
                .boxed()
                .map(eventTime -> {
                    int objectID = this.nextObjectID();
                    return this.getEvent(objectID, eventTime, createEventsForAllStates.get(objectID));
                })
                .collect(toList());
        return new GeneratedData(Stream.concat(createEvents.stream(), collect.stream()).collect(toList()));
    }

    private Map<Integer, QuestionCreated> generateCreateEvents(int maxObjectID) {
        return IntStream.range(0, maxObjectID)
                .boxed().map(id -> new QuestionCreated(market, id, this.getRandomContent(), startingDateTime.plusMinutes(id)))
                .collect(Collectors.toMap(QuestionCreated::getQuestionID, Function.identity()));
    }

    private QuestionModifier getEvent(int objectID, Integer eventID, QuestionCreated initialObjectCreation) {

        ZonedDateTime now = startingDateTime.plusMinutes(eventID);
        Integer next = this.eventsPercentages.next();

        switch (next) {
            case 1:
                return new QuestionCreated(this.market, objectID, initialObjectCreation.getContent(), initialObjectCreation.getCreateDate());
            case 2:
                return new QuestionUpdated(this.market, objectID, getRandomContent(), now);
            case 3:
                return new QuestionDeleted(this.market, objectID, now);
        }
        throw new RuntimeException("Generating not expected event");
    }

    private String getRandomContent() {
        return RandomStringUtils.random(new Random().nextInt(100));
    }

    private int nextObjectID() {
        return new Random().nextInt(this.maxObjectID);
    }
}
