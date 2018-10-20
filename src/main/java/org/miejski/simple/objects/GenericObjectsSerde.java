package org.miejski.simple.objects;

import org.miejski.simple.objects.events.ObjectCreation;
import org.miejski.simple.objects.events.ObjectDelete;
import org.miejski.simple.objects.events.ObjectUpdate;
import org.miejski.simple.objects.serdes.GenericSerde;

import java.util.HashMap;
import java.util.Map;

public class GenericObjectsSerde { // TODO replace with JSONSerde

    public static GenericSerde build() {
        Map<String, Class> serializers= new HashMap<>();
        serializers.put(ObjectCreation.class.getSimpleName(), ObjectCreation.class);
        serializers.put(ObjectUpdate.class.getSimpleName(), ObjectUpdate.class);
        serializers.put(ObjectDelete.class.getSimpleName(), ObjectDelete.class);

        return new GenericSerde(serializers);
    }
}
