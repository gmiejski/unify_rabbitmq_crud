package org.miejski.questions

import com.fasterxml.jackson.databind.ObjectMapper
import org.miejski.questions.source.*
import org.miejski.questions.source.create.SourceQuestionCreateProducer
import org.miejski.questions.source.delete.SourceQuestionDeletedProducer
import org.miejski.questions.source.rabbitmq.RabbitMQJsonProducer
import org.miejski.questions.source.update.SourceQuestionUpdatedProducer

class RunRabbitFixedCountProducer {

    private var objectMapper: ObjectMapper = QuestionObjectMapper.build()

    private var producers: List<MultiSourceEventProducer<out Any>> = emptyList()

    private lateinit var rabbitProducers: List<RabbitMQJsonProducer>

    fun setup(market: String, maxQuestionID: Int) {
        val createQuestion = RabbitMQJsonProducer.localRabbitMQProducer(objectMapper, RabbitMQJsonProducer.QUESTION_CREATED_QUEUE)
        val updateQuestion = RabbitMQJsonProducer.localRabbitMQProducer(objectMapper, RabbitMQJsonProducer.QUESTION_UPDATED_QUEUE)
        val deleteQuestion = RabbitMQJsonProducer.localRabbitMQProducer(objectMapper, RabbitMQJsonProducer.QUESTION_DELETED_QUEUE)

        this.rabbitProducers = listOf(createQuestion, updateQuestion, deleteQuestion)

        this.producers = listOf(
            MultiCreateSourceEventProducer(SourceQuestionCreateProducer(market, RandomQuestionIDProvider(maxQuestionID))),
            MultiSourceEventProducer(SourceQuestionUpdatedProducer(market, RandomQuestionIDProvider(maxQuestionID))),
            MultiSourceEventProducer(SourceQuestionDeletedProducer(market, RandomQuestionIDProvider(maxQuestionID))))
    }

    fun startAll(eventsCount: Int) {
        this.rabbitProducers.forEach { it.setup() }
        for (i in 0..this.producers.size-1) {
            this.producers[i].create(eventsCount).forEach { this.rabbitProducers[i].accept(it) }
        }
    }

    fun stopAll() {
        rabbitProducers.forEach { it.close() }
    }
}

fun main(args: Array<String>) {

    val runner = RunRabbitFixedCountProducer()
    runner.setup("us", 10000)
    runner.startAll(10000)
    runner.stopAll()
}