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
import java.util.stream.IntStream;

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

    private List<EventWithDate> events;

    public GeneratedData(List<EventWithDate> collect) {
        this.events = collect;
    }

    public List<ObjectModifier> getEvents() {
        return this.events.stream().map(EventWithDate::getEvent).collect(toList());
    }

    public Map<String, ObjectState> finalState() {
        Map<String, ObjectState> finalStates = this.events.stream()
                .collect(groupingBy(x -> x.getEvent().ID()))
                .entrySet().stream()
                .collect(toMap(Map.Entry::getKey, kv -> this.toObjectState(kv.getValue().stream().map(EventWithDate::getEvent).collect(toList()))));
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
        return this.events.stream().filter(x -> x.getEvent().ID().equals(ID)).map(EventWithDate::getEvent).collect(toList());
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
        List<EventWithDate> collect = IntStream.range(0, eventsCount)
                .boxed()
                .map(v -> new EventWithDate(this.getEvent(v), v))
                .collect(toList());
        return new GeneratedData(collect);
    }

    private ObjectModifier getEvent(Integer eventID) {

        ZonedDateTime now = startingDateTime.plusMinutes(eventID);
        Integer next = this.eventsPercentages.next();
        String objectID = String.valueOf(new Random().nextInt(this.maxObjectID));

        switch (next) {
            case 1:
                return new ObjectCreation(objectID, new Random().nextInt(100), now);
            case 2:
                return new ObjectUpdate(objectID, new Random().nextInt(100), now);
            case 3:
                return new ObjectDelete(objectID, now);
        }
        throw new RuntimeException("Generating not expected event");
    }
}
