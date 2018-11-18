package org.miejski.questions.domain

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.miejski.questions.QuestionID
import org.miejski.questions.QuestionsStateTopology
import org.miejski.questions.state.QuestionState

class QuestionsService(val streams: KafkaStreams) {
    fun get(market: String, id: Int): QuestionState {
        val store = streams.store(QuestionsStateTopology.QUESTIONS_STORE_NAME, QueryableStoreTypes.keyValueStore<String, QuestionState>())
        return store.get(QuestionID.from(market, id)) ?: throw QuestionNotFound(market, id)
    }

    fun getAll(market: String): List<QuestionState> {
        val store = streams.store(QuestionsStateTopology.QUESTIONS_STORE_NAME, QueryableStoreTypes.keyValueStore<String, QuestionState>())
        return store.all().asSequence()
            .filter { it.value.market.equals(market) }
            .map { it.value }.toList()
    }
}