package org.miejski.questions.domain

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.miejski.questions.QuestionID
import org.miejski.questions.QuestionStateEntity
import org.miejski.questions.QuestionsStateTopology
import org.miejski.questions.state.QuestionState
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component(value = "questionStateListener")
class Listener @Autowired constructor(val repo: QuestionStateRepository, val settings: QuestionServiceSettings ) {


    @KafkaListener(
        id = "#{__listener.clientId()}",
        topics = arrayOf(QuestionsStateTopology.READ_ONLY_STATE_TOPIC),
        clientIdPrefix = "#{__listener.clientId()}",
        errorHandler = "org.miejski.questions.domain.ConsumerError"
    )
    fun listen(record: ConsumerRecord<String, QuestionState>) {
        println(record.key())
        val questionStateEntity = QuestionStateEntity()
        questionStateEntity.id = QuestionID.from(record.key()).questionID
        questionStateEntity.content = record.value().content
        questionStateEntity.market = record.value().market
        questionStateEntity.deleteDate = record.value().deleteDate
        questionStateEntity.lastModification = record.value().lastModification
        repo.save(questionStateEntity)
    }

    fun clientId(): String {
        return settings.applicationID
    }

}