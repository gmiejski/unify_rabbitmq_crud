package org.miejski.questions

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.miejski.questions.source.RandomQuestionIDProvider
import org.miejski.questions.source.create.SourceQuestionCreateProducer
import org.miejski.questions.state.QuestionState
import org.miejski.simple.objects.serdes.GenericJSONSer
import java.util.*

class ROTopicProducer {
    private var kafkaProducer: KafkaProducer<String, QuestionState>

    init {
        val kafkaProps = Properties()
        kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
        kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GenericJSONSer::class.java)
        kafkaProps.put(ProducerConfig.ACKS_CONFIG, "0")
        this.kafkaProducer = KafkaProducer(kafkaProps)
    }

    private fun send(questionState: QuestionState) {
        val record: ProducerRecord<String, QuestionState>? = ProducerRecord(QuestionsStateTopology.READ_ONLY_STATE_TOPIC, questionState.id(), questionState)
        try {
            kafkaProducer.send(record)
        } catch (e: Exception) {
            e.printStackTrace()
            kafkaProducer.close()
            throw e
        }
    }

    fun start(count: Int) {
        val eventProducer = SourceQuestionCreateProducer("us", RandomQuestionIDProvider(1000))
        for (i in 1..count) {
            this.send(eventProducer.create().doSomething(QuestionState()))
        }
        eventProducer.create()
    }

    fun close() {
        kafkaProducer.close()
    }
}

fun main(args: Array<String>) {
    val producer = ROTopicProducer()
    producer.start(10)
    producer.close()
}