package org.miejski.simple.objects.events;

import org.miejski.simple.objects.ObjectState;

public class ObjectDelete implements ObjectModifier {

    @Override
    public ObjectState doSomething(ObjectState obj) {
        if (obj == null) {
            return new ObjectState(ObjectState.NOT_SET, true);
        }
        return new ObjectState(obj.getValue(), true);
    }
}
