package org.miejski;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;

public interface TopologyBuilder {
    default Topology buildTopology() {
        return buildTopology(new StreamsBuilder());
    }

    Topology buildTopology(StreamsBuilder builder);
}
