package org.miejski.simple.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

import static com.google.common.base.MoreObjects.toStringHelper;


public class ObjectState {

    public static boolean isInitialized(ObjectState state) {
        return state != null && state.isInitialized();
    }

    public static boolean idNotMatching(ObjectState state, String ID) {
        return state != null && state.ID() != null && !ID.equals(state.ID());
    }

    public static int NOT_SET = Integer.MIN_VALUE;

    private String ID;
    private int value = 0;
    private boolean isDeleted = false;
    private ZonedDateTime lastModification = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());

    public ObjectState(String id, int value) {
        this(id, value, false);
    }

    public ObjectState(String id, int value, boolean isDeleted) {
        this.ID = id;
        this.value = value;
        this.isDeleted = isDeleted;
    }

    public ObjectState() {
    }

    @JsonProperty(value = "ID")
    public String ID() {
        return this.ID;
    }

    public int getValue() {
        return value;
    }

    @JsonIgnore
    public boolean isInitialized() {
        return this.ID != null;
    }

    @JsonProperty(value = "isDeleted")
    public boolean isDeleted() {
        return isDeleted;
    }

    public ObjectState withValue(int value) {
        return new ObjectState(this.ID, value).deleted(this.isDeleted());
    }

    private ObjectState deleted(boolean deleted) {
        return new ObjectState(this.ID, value, deleted);
    }

    public ObjectState withLastModification(ZonedDateTime lastModification) {
        ObjectState objectState = new ObjectState(this.ID, value, this.isDeleted);
        objectState.lastModification = lastModification;
        return objectState;
    }

    public ZonedDateTime getLastModification() {
        return lastModification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectState that = (ObjectState) o;
        if (!Objects.equals(ID, that.ID)) {
            return false;
        }
        if (this.isDeleted() && that.isDeleted()) {
            return true;
            // TODO Would be great to have this, but this would fail commutative delete before create :(
            // return this.getValue() == that.getValue();
        }
        if (this.isDeleted() || that.isDeleted()) {
            return false;
        }
        return this.getValue() == that.getValue();
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("ID", ID)
                .add("value", value)
                .add("isDeleted", isDeleted)
                .add("lastModification", lastModification)
                .toString();
    }
}
