package org.miejski.questions.domain

import org.miejski.questions.state.QuestionState
import org.springframework.stereotype.Service

@Service
class QuestionsService(val repo: QuestionStateRepository) {
    fun get(market: String, id: Int): QuestionState {
        val findById = repo.findByMarketAndId(market, id)
        return findById.orElseThrow { QuestionNotFound(market, id) }.toQuestionState()
    }

    fun getAll(market: String): List<QuestionState> {
        return repo.findByMarket(market).map { it.toQuestionState() }
    }

    fun count(): Long {
        return repo.count()
    }
}