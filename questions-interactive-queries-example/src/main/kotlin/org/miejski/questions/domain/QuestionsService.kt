package org.miejski.questions.domain

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.miejski.questions.QuestionID
import org.miejski.questions.QuestionsStateTopology
import org.miejski.questions.state.QuestionState

class QuestionsService(val streams: KafkaStreams) {
    fun get(market: String, id: Int): QuestionState {
        val store = getStore()
        return store.get(QuestionID.from(market, id)) ?: throw QuestionNotFound(market, id)
    }

    fun getAll(market: String): List<QuestionState> {
        val iterator = getStore().all()
        val all = iterator.asSequence()
            .filter { it.value.market.equals(market) }
            .map { it.value }.toList()
        iterator.close()
        return all
    }

    fun count(): Int {
        val iterator = getStore().all()
        val result = iterator.asSequence().count()
        iterator.close()
        return result
    }

    private fun getStore(): ReadOnlyKeyValueStore<String, QuestionState> {
        return streams.store(QuestionsStateTopology.QUESTIONS_STORE_NAME, QueryableStoreTypes.keyValueStore())
    }
}