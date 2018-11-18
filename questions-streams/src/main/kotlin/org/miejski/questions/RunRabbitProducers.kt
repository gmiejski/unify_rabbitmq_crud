package org.miejski.questions

import com.fasterxml.jackson.databind.ObjectMapper
import org.miejski.questions.events.QuestionModifier
import org.miejski.questions.source.GeneratingBusProducer
import org.miejski.questions.source.RandomQuestionIDProvider
import org.miejski.questions.source.create.SourceQuestionCreateProducer
import org.miejski.questions.source.delete.SourceQuestionDeletedProducer
import org.miejski.questions.source.rabbitmq.RabbitMQJsonProducer
import org.miejski.questions.source.update.SourceQuestionUpdatedProducer
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class RunRabbitProducers {

    private var objectMapper: ObjectMapper = QuestionObjectMapper.build()

    private var producers: List<GeneratingBusProducer<out QuestionModifier>> = emptyList()
    private var completed: List<CompletableFuture<Boolean>> = emptyList()

    private lateinit var rabbitProducers: List<RabbitMQJsonProducer>

    fun createAll(market: String, maxQuestionID: Int) {

        val createQuestion = RabbitMQJsonProducer.localRabbitMQProducer(objectMapper, RabbitMQJsonProducer.QUESTION_CREATED_QUEUE)
        val updateQuestion = RabbitMQJsonProducer.localRabbitMQProducer(objectMapper, RabbitMQJsonProducer.QUESTION_UPDATED_QUEUE)
        val deleteQuestion = RabbitMQJsonProducer.localRabbitMQProducer(objectMapper, RabbitMQJsonProducer.QUESTION_DELETED_QUEUE)

        this.rabbitProducers = listOf(createQuestion, updateQuestion, deleteQuestion)

        this.producers = listOf(
            GeneratingBusProducer(SourceQuestionCreateProducer(market, RandomQuestionIDProvider(maxQuestionID)), createQuestion),
            GeneratingBusProducer(SourceQuestionUpdatedProducer(market, RandomQuestionIDProvider(maxQuestionID)), updateQuestion),
            GeneratingBusProducer(SourceQuestionDeletedProducer(market, RandomQuestionIDProvider(maxQuestionID)), deleteQuestion)
        )
    }

    fun startAll() {
        this.rabbitProducers.forEach { it.setup() }
        this.completed = this.producers.map { it.start() }
    }

    fun stopAll() {
        this.producers.forEach { it.stop() }
        val allOf = CompletableFuture.allOf(*this.completed.toTypedArray())
        allOf.get(10, TimeUnit.SECONDS)
    }
}


fun main(args: Array<String>) {

    val runner = RunRabbitProducers()
    runner.createAll("us", 1000)
    runner.startAll()

    Runtime.getRuntime().addShutdownHook(object : Thread("shutdown-hook") {
        override fun run() {
            runner.stopAll()
        }
    })
}