package org.miejski.simple.objects.events;

import org.miejski.simple.objects.ObjectState;

public class ObjectCreation implements ObjectModifier {

    private String ID;
    private int value;
    // TODO add lastModificationDate
    public ObjectCreation() {
    }

    public ObjectCreation(String ID, int value) {
        this.ID = ID;
        this.value = value;
    }

    @Override
    public ObjectState doSomething(ObjectState obj) {
        if (obj != null && obj.ID() != null && !this.ID.equals(obj.ID())) {
            throw new RuntimeException("Wrong ID");
        }
        if (obj != null && obj.isInitialized()) {
            return obj;
        }
        return new ObjectState(value).withID(this.ID());
    }

    @Override
    public String ID() {
        return this.ID;
    }
}
