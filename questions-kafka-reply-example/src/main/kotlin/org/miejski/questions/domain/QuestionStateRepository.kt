package org.miejski.questions.domain

import org.miejski.questions.QuestionStateEntity
import org.springframework.data.repository.CrudRepository
import java.util.*

interface QuestionStateRepository : CrudRepository<QuestionStateEntity, Int> {

    fun findByMarketAndId(market: String, id: Int): Optional<QuestionStateEntity>
    fun findByMarket(market: String): List<QuestionStateEntity>

}