package org.miejski.simple.objects.events;

import org.miejski.simple.objects.ObjectState;

import java.time.ZonedDateTime;

public class ObjectUpdate implements ObjectModifier {

    private int value;
    private String ID;
    private ZonedDateTime updateDate;

    public ObjectUpdate() {
    }

    public ObjectUpdate(String ID, int value, ZonedDateTime updateDate) {
        this.ID = ID;
        this.value = value;
        this.updateDate = updateDate;
    }

    @Override
    public ObjectState doSomething(ObjectState obj) {
        if (obj != null && obj.ID()!= null && !this.ID.equals(obj.ID())) {
            throw new RuntimeException("Wrong ID");
        }
        if (obj == null || !obj.isInitialized()) {
            return new ObjectState(value).withID(this.ID());// TODO move ID to constructor && get rid of initialized (base on ID instead)
        }
        return obj.withValue(value);
    }

    @Override
    public String ID() {
        return this.ID;
    }
}
