package org.miejski.simple.objects.events;

import org.miejski.simple.objects.IdNotMatchingException;
import org.miejski.simple.objects.ObjectState;

import java.time.ZonedDateTime;

public class ObjectDelete implements ObjectModifier {

    private String id;
    private ZonedDateTime deleteDate;

    public ObjectDelete() {
    }

    public ObjectDelete(String id, ZonedDateTime deleteDate) {
        this.id = id;
        this.deleteDate = deleteDate;
    }

    @Override
    public ObjectState doSomething(ObjectState obj) {
        if (ObjectState.idNotMatching(obj, this.id)) {
            throw new IdNotMatchingException("Wrong id");
        }
        if (!ObjectState.isInitialized(obj)) {
            return new ObjectState(id, ObjectState.NOT_SET, true);
        }
        return new ObjectState(id, obj.getValue(), true).withLastModification(obj.getLastModification());
    }

    @Override
    public String ID() {
        return this.id;
    }
}
