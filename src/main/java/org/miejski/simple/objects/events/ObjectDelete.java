package org.miejski.simple.objects.events;

import org.miejski.simple.objects.ObjectState;

import java.time.ZonedDateTime;

public class ObjectDelete implements ObjectModifier {

    private String ID;
    private ZonedDateTime deleteDate;

    public ObjectDelete() {
    }

    public ObjectDelete(String ID, ZonedDateTime deleteDate) {
        this.ID = ID;
        this.deleteDate = deleteDate;
    }

    @Override
    public ObjectState doSomething(ObjectState obj) {
        if (obj != null && obj.ID() != null && !this.ID.equals(obj.ID())) {
            throw new RuntimeException("Wrong ID");
        }
        if (obj == null || !obj.isInitialized()) {
            return new ObjectState(ObjectState.NOT_SET, true).withID(this.ID());
        }
        return new ObjectState(obj.getValue(), true).withID(this.ID());
    }

    @Override
    public String ID() {
        return this.ID;
    }
}
