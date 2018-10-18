package org.miejski.simple.objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ObjectState {

    public static int NOT_SET = Integer.MIN_VALUE;

    int value = 0;
    boolean isDeleted = false;
    boolean isInitialized = false;

    public ObjectState(int value) {
        this(value, false);
    }

    public ObjectState(int value, boolean isDeleted) {
        this.value = value;
        this.isDeleted = isDeleted;
    }

    public ObjectState() {
    }

    public int getValue() {
        return value;
    }

    @JsonProperty(value="isInitialized")
    public boolean isInitialized() {
        return isInitialized;
    }

    public ObjectState initialize() {
        isInitialized = true;
        return this;
    }
}
