package org.miejski.simple.objects;

import org.apache.kafka.streams.kstream.ValueJoiner;
import org.miejski.simple.objects.events.ObjectModifier;

public class ObjectsJoiner implements ValueJoiner<ObjectState, ObjectModifier, ObjectState> {
    @Override
    public ObjectState apply(ObjectState state, ObjectModifier modifier) {
        return modifier.doSomething(state);
    }
}
