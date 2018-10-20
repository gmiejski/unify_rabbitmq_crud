package org.miejski.simple.objects.events;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.miejski.simple.objects.ObjectState;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;


public class CommutativeEventsTest {

    private String ID = "1";
    private final ZonedDateTime startingDateTime = ZonedDateTime.of(2018, 10, 10, 0, 0, 0, 0, ZoneId.systemDefault());
    ObjectCreation objectCreation = new ObjectCreation(ID, 10, startingDateTime);
    ObjectUpdate objectUpdate = new ObjectUpdate(ID, 20, startingDateTime.plusMinutes(1));
    ObjectDelete objectDelete = new ObjectDelete(ID, startingDateTime.plusMinutes(2));

    @Test
    void CreateAndUpdate() {
        ObjectState state1 = apply(objectCreation, objectUpdate);
        ObjectState state2 = apply(objectUpdate, objectCreation);

        Assertions.assertEquals(state1, state2);
    }

    @Test
    void CreateAndDelete() {
        ObjectState state1 = apply(objectCreation, objectDelete);
        ObjectState state2 = apply(objectDelete, objectCreation);

        Assertions.assertEquals(state1, state2);
    }

    @Test
    void UpdateAndDelete() {
        ObjectState state1 = apply(objectDelete, objectUpdate);
        ObjectState state2 = apply(objectUpdate, objectDelete);

        Assertions.assertEquals(state1, state2);
    }


    @Test
    void DoubleCreate() {
        ObjectState state1 = apply(objectCreation, objectUpdate, objectCreation);
        ObjectState state2 = apply(objectCreation, objectUpdate);

        Assertions.assertEquals(state1, state2);
    }

    @Test
    void AllThreeCommutative() {
        List<ObjectState> all = Arrays.asList(
                apply(objectCreation, objectUpdate, objectDelete),
                apply(objectCreation, objectDelete, objectUpdate),
                apply(objectDelete, objectUpdate, objectCreation),
                apply(objectDelete, objectCreation, objectUpdate),
                apply(objectUpdate, objectDelete, objectCreation),
                apply(objectUpdate, objectCreation, objectDelete));

        for (int i = 0; i < all.size() - 1; i++) {
            for (int j = i + 1; j < all.size(); j++) {
                Assertions.assertEquals(all.get(i), all.get(j));
            }
        }
    }

    private ObjectState apply(ObjectModifier... events) {
        ObjectState result = new ObjectState();
        for (ObjectModifier event : events) {
            result = event.doSomething(result);
        }
        return result;
    }
}



