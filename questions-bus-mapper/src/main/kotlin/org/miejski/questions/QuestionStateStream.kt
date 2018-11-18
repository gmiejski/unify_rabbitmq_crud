package org.miejski.questions

import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.miejski.questions.bus2kafka.Bus2KafkaMappingTopology
import java.util.*

fun main(args: Array<String>) {
    val streamsBuilder = StreamsBuilder()
    Bus2KafkaMappingTopology().buildTopology(streamsBuilder)
    QuestionsStateTopology().buildTopology(streamsBuilder)

    val runner = KafkaStreamRunner(streamsBuilder)
    val props = Properties()
    props[StreamsConfig.APPLICATION_ID_CONFIG] = "whole-questions-topology"
    props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092,localhost:9093,localhost:9094"
    runner.run(props)
}
