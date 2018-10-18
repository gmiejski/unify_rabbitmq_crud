package org.miejski.simple.objects.events;

import org.miejski.simple.objects.ObjectState;

public class ObjectCreation implements ObjectModifier {

    private int value;

    public ObjectCreation() {
    }

    public ObjectCreation(int value) {
        this.value = value;
    }

    @Override
    public ObjectState doSomething(ObjectState obj) {
        if (obj != null && obj.isInitialized()) {
            return obj;
        }
        return new ObjectState(value).initialize();
    }
}
