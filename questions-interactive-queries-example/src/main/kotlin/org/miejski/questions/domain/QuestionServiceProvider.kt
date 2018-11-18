package org.miejski.questions.domain

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.miejski.questions.QuestionStateRunner
import org.miejski.questions.QuestionsStateReaderTopology
import org.miejski.questions.QuestionsStateTopology
import org.miejski.questions.bus2kafka.Bus2KafkaMappingTopology
import org.springframework.beans.factory.FactoryBean
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.*
import java.util.concurrent.CountDownLatch

@Component
class QuestionServiceProvider constructor(val settings: QuestionServiceSettings) : FactoryBean<QuestionsService> {

    override fun getObject(): QuestionsService? {
        val props = Properties()
        props[StreamsConfig.APPLICATION_ID_CONFIG] = settings.applicationID
        props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = settings.bootstrapServers

        val streamsBuilder = StreamsBuilder()
        QuestionsStateReaderTopology().buildTopology(streamsBuilder)

        val streams = KafkaStreams(streamsBuilder.build(), props)
        this.start(streams)
        streams.waitForState(KafkaStreams.State.RUNNING, Duration.ofSeconds(10))

        return QuestionsService(streams)
    }

    private fun start(streams: KafkaStreams) {
        val latch = CountDownLatch(1)
        QuestionStateRunner.addShutdownHook(streams, latch)

        Thread {
            streams.start()
            latch.await()
        }.start()
    }

    override fun getObjectType(): Class<*>? {
        return QuestionsService::class.java
    }

}

private fun KafkaStreams.waitForState(expectedState: KafkaStreams.State, waitTime: Duration) {
    // TODO waitedSoFar!
    while (this.state() != expectedState) {
        println("waiting for streams to start")
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
            System.exit(1)
        }
    }
}

