package org.miejski.simple.objects.events;

import org.miejski.simple.objects.ObjectState;

public class ObjectDelete implements ObjectModifier {

    public ObjectDelete() {
    }

    @Override
    public ObjectState doSomething(ObjectState obj) {
        if (obj == null) {
            return new ObjectState(ObjectState.NOT_SET, true).initialize();
        }
        return new ObjectState(obj.getValue(), true).initialize();
    }
}
