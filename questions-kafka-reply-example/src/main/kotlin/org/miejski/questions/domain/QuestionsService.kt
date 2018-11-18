package org.miejski.questions.domain

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.miejski.questions.QuestionID
import org.miejski.questions.QuestionsStateTopology
import org.miejski.questions.state.QuestionState
import org.springframework.stereotype.Service

@Service
class QuestionsService() {
    fun get(market: String, id: Int): QuestionState {
        throw NotImplementedError()
    }

    fun getAll(market: String): List<QuestionState> {
        throw NotImplementedError()
    }

    fun count(): Int {
        return 1
    }

}