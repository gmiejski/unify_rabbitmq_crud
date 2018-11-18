package org.miejski.simple.objects.events;

import org.miejski.exceptions.IdNotMatchingException;
import org.miejski.simple.objects.ObjectState;

import java.time.ZonedDateTime;

public class ObjectCreation implements ObjectModifier {

    private String id;
    private int value;
    private ZonedDateTime createDate;

    public ObjectCreation(String id, int value, ZonedDateTime createDate) {
        this.id = id;
        this.value = value;
        this.createDate = createDate;
    }

    public ObjectCreation() {
    }

    @Override
    public ObjectState doSomething(ObjectState obj) {
        if (ObjectState.idNotMatching(obj, this.id)) {
            throw new IdNotMatchingException("Wrong id");
        }
        if (ObjectState.isInitialized(obj)) {
            return obj;
        }
        return new ObjectState(id, value).withLastModification(this.createDate);
    }

    @Override
    public String ID() {
        return this.id;
    }

    public ZonedDateTime getCreateDate() {
        return createDate;
    }

    public int getValue() {
        return this.value;
    }
}
