package org.miejski.questions.controller

import org.miejski.questions.state.QuestionState


data class QuestionStateDTO(val questionID: Int, val market: String, val content: String, val b: Boolean)

fun QuestionState.toDto(): QuestionStateDTO {
    return QuestionStateDTO(this.questionID, this.market, this.content, this.isDeleted())
}