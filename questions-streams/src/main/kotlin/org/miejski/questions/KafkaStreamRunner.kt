package org.miejski.questions

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import java.util.*
import java.util.concurrent.CountDownLatch


class KafkaStreamRunner constructor(val builder: StreamsBuilder) {

    fun run(props: Properties) {
        val streams = KafkaStreams(builder.build(), props)
        val latch = CountDownLatch(1)
        QuestionStateRunner.addShutdownHook(streams, latch)

        try {
            streams.start()
            latch.await()
        } catch (e: Throwable) {
            System.exit(1)
        }
    }
}