package org.miejski.simple.unify;

import org.apache.kafka.streams.kstream.ValueJoiner;

import static org.miejski.simple.unify.SimpleUnifyTopology.DELETED_VALUE;

public class NumbersJoiner implements ValueJoiner<Integer, Integer, Integer> {

    @Override
    public Integer apply(Integer newValue, Integer state) {
        if (state == null) {
            return newValue;
        }
        if (DELETED_VALUE == state) {
            return DELETED_VALUE;
        }
        return state + newValue;
    }
}