package org.miejski;

import org.apache.kafka.streams.Topology;

public interface TopologyBuilder {
    Topology buildTopology();
}
