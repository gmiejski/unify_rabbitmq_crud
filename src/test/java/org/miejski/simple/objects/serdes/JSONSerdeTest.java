package org.miejski.simple.objects.serdes;

import org.apache.kafka.common.serialization.Serde;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.miejski.simple.objects.events.ObjectDelete;
import org.miejski.simple.objects.events.ObjectModifier;

import java.time.ZonedDateTime;

class JSONSerdeTest {

    @Test
    void shouldUseGenericJSONSerde() {
        ObjectDelete initial = new ObjectDelete("someKey", ZonedDateTime.now());

        Serde<ObjectModifier> serde = JSONSerde.serde2(ObjectDelete.class);

        byte[] serialize = serde.serializer().serialize("", initial);

        ObjectModifier deserialize = serde.deserializer().deserialize("", serialize);

        Assertions.assertEquals(initial, deserialize);
    }
}