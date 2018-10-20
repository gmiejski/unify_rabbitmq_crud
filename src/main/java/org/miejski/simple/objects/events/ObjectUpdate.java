package org.miejski.simple.objects.events;

import org.miejski.simple.objects.IdNotMatchingException;
import org.miejski.simple.objects.ObjectState;

import java.time.ZonedDateTime;

public class ObjectUpdate implements ObjectModifier {

    private int value;
    private String id;
    private ZonedDateTime updateDate;

    public ObjectUpdate() {
    }

    public ObjectUpdate(String id, int value, ZonedDateTime updateDate) {
        this.id = id;
        this.value = value;
        this.updateDate = updateDate;
    }

    @Override
    public ObjectState doSomething(ObjectState state) {
        if (ObjectState.idNotMatching(state, this.id)) {
            throw new IdNotMatchingException("Wrong id");
        }
        if (!ObjectState.isInitialized(state)) {
            return new ObjectState(id, value).withLastModification(this.updateDate);
        }
        if (this.updateDate.isBefore(state.getLastModification())) {
            return state;
        }

        return state.withValue(value).withLastModification(updateDate);
    }

    @Override
    public String ID() {
        return this.id;
    }

    public ZonedDateTime getUpdateDate() {
        return updateDate;
    }
}
