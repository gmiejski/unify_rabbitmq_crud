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
        if (obj == null) {
            return new ObjectState(this.value);
        }
        return new ObjectState(value, false);
    }
}
