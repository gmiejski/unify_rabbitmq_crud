package org.miejski.simple.objects.consistecy;

import org.miejski.simple.objects.ObjectState;
import org.miejski.simple.objects.events.ObjectCreation;
import org.miejski.simple.objects.events.ObjectDelete;
import org.miejski.simple.objects.events.ObjectModifier;
import org.miejski.simple.objects.events.ObjectUpdate;

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

class EventWithDate {
    private final ObjectModifier event;
    private final int time;

    public EventWithDate(ObjectModifier event, int time) {
        this.event = event;
        this.time = time;
    }

    public ObjectModifier getEvent() {
        return event;
    }

    public int getTime() {
        return time;
    }
}

class GeneratedData {

    private List<ObjectModifier> events;

    public GeneratedData(List<ObjectModifier> collect) {
        this.events = collect;
    }

    public List<ObjectModifier> getEvents() {
        return this.events;
    }

    public Map<String, ObjectState> finalState() {
        Map<String, ObjectState> finalStates = this.events.stream()
                .collect(groupingBy(ObjectModifier::ID))
                .entrySet().stream()
                .collect(toMap(Map.Entry::getKey, kv -> this.toObjectState(kv.getValue())));
        return finalStates;
    }

    private ObjectState toObjectState(List<ObjectModifier> events) {
        ObjectState result = new ObjectState();
        for (ObjectModifier event : events) {
            result = event.doSomething(result);
        }
        return result;
    }

    public List<ObjectModifier> getEventsFor(String ID) {
        return this.events.stream().filter(event -> event.ID().equals(ID)).collect(toList());
    }
}

public class Generator {

    private final ZonedDateTime startingDateTime = ZonedDateTime.of(2018, 10, 10, 0, 0, 0, 0, ZoneId.systemDefault());
    private final EventsPercentages eventsPercentages;
    private final int eventsCount;
    private int maxObjectID;

    public Generator(EventsPercentages eventsPercentages, int eventsCount, int maxObjectID) {
        this.eventsPercentages = eventsPercentages;
        this.eventsCount = eventsCount;
        this.maxObjectID = maxObjectID;
    }

    public GeneratedData generate() {

        Map<String, ObjectCreation> createEventsForAllStates = generateCreateEvents(this.maxObjectID);
        List<ObjectModifier> createEvents = createEventsForAllStates.values().stream().map(x -> (ObjectModifier) x).collect(toList());
        List<ObjectModifier> collect = IntStream.range(createEventsForAllStates.size(), eventsCount)
                .boxed()
                .map(eventTime -> {
                    String objectID = this.nextObjectID();
                    return new EventWithDate(this.getEvent(objectID, eventTime, createEventsForAllStates.get(objectID)), eventTime);
                })
                .map(EventWithDate::getEvent)
                .collect(toList());
        return new GeneratedData(Stream.concat(createEvents.stream(), collect.stream()).collect(toList()));
    }

    private Map<String, ObjectCreation> generateCreateEvents(int maxObjectID) {
        return IntStream.range(0, maxObjectID)
                .boxed().map(id -> new ObjectCreation(String.valueOf(id), this.getRandomValue(), startingDateTime.plusMinutes(id)))
                .collect(Collectors.toMap(ObjectCreation::ID, Function.identity()));
    }

    private ObjectModifier getEvent(String objectID, Integer eventID, ObjectCreation initialObjectCreation) {

        ZonedDateTime now = startingDateTime.plusMinutes(eventID);
        Integer next = this.eventsPercentages.next();

        switch (next) {
            case 1:
                return new ObjectCreation(objectID, initialObjectCreation.getValue(), initialObjectCreation.getCreateDate());
            case 2:
                return new ObjectUpdate(objectID, getRandomValue(), now);
            case 3:
                return new ObjectDelete(objectID, now);
        }
        throw new RuntimeException("Generating not expected event");
    }

    private int getRandomValue() {
        return new Random().nextInt(100);
    }

    private String nextObjectID() {
        return String.valueOf(new Random().nextInt(this.maxObjectID));
    }
}
