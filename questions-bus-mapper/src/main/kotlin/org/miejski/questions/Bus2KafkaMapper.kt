package org.miejski.questions

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsConfig
import org.miejski.questions.bus2kafka.Bus2KafkaMappingTopology
import java.util.*
import java.util.concurrent.CountDownLatch

fun main(args: Array<String>) {
    val props = Properties()
    props[StreamsConfig.APPLICATION_ID_CONFIG] = "question-states"
    props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092,localhost:9093,localhost:9094"
    val topology = Bus2KafkaMappingTopology().buildTopology()

    val streams = KafkaStreams(topology, props)
    val latch = CountDownLatch(1)

    QuestionStateRunner.addShutdownHook(streams, latch)

    try {
        streams.start()
        latch.await()
    } catch (e: Throwable) {
        System.exit(1)
    }
}