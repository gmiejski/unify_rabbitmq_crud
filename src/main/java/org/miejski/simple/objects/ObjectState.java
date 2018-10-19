package org.miejski.simple.objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ObjectState {

    public static int NOT_SET = Integer.MIN_VALUE;

    private String ID;
    private int value = 0;
    private boolean isDeleted = false;
    private boolean isInitialized = false;

    public ObjectState(int value) {
        this(value, false);
    }

    public ObjectState(int value, boolean isDeleted) {
        this.value = value;
        this.isDeleted = isDeleted;
        this.isInitialized = true;
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

    @JsonProperty(value = "isInitialized")
    public boolean isInitialized() {
        return isInitialized;
    }

    @JsonProperty(value = "isDeleted")
    public boolean isDeleted() {
        return isDeleted;
    }

    public ObjectState withID(String id) {
        if (this.ID == null || this.ID.isEmpty()) {
            this.ID = id;
        } else if (!this.ID.equals(id)) {
            throw new RuntimeException("Wrong objects IDs are being merged!!!!!");
        }
        return this;
    }

    public ObjectState withValue(int value) {
        return new ObjectState(value).withID(this.ID).deleted(this.isDeleted());
    }

    private ObjectState deleted(boolean deleted) {
        return new ObjectState(value, deleted).withID(this.ID);
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
}
