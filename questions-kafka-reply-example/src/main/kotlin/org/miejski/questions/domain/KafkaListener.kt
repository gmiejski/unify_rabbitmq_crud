package org.miejski.questions.domain

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.miejski.questions.QuestionsStateTopology
import org.miejski.questions.state.QuestionState
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class Listener {

    @KafkaListener(
        id = "restID",
        topics = arrayOf(QuestionsStateTopology.READ_ONLY_STATE_TOPIC),
        clientIdPrefix = "myClientId",
        errorHandler = "org.miejski.questions.domain.ConsumerError"
        )
    fun listen(a: ConsumerRecord<String, QuestionState>) {
        println(a.key())
    }
}