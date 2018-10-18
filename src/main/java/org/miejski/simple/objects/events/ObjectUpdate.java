package org.miejski.simple.objects.events;

import org.miejski.simple.objects.ObjectState;

public class ObjectUpdate implements ObjectModifier {

    private int value;

    public ObjectUpdate() {
    }

    public ObjectUpdate(int value) {
        this.value = value;
    }

    @Override
    public ObjectState doSomething(ObjectState obj) {
        if (obj == null || !obj.isInitialized()) {
            return new ObjectState(this.value).initialize();
        }
        return new ObjectState(value).initialize();
    }
}
