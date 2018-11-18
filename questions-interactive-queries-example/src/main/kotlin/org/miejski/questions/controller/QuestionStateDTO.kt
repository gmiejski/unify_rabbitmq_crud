package org.miejski.questions.controller

import org.miejski.questions.state.QuestionState
import java.time.ZonedDateTime

data class QuestionStateDTO(val questionID: Int, val market: String, val content: String?, val deletedDate: ZonedDateTime?, val lastModificationDate: ZonedDateTime?)

fun QuestionState.toDto(): QuestionStateDTO {
    return QuestionStateDTO(this.questionID, this.market, this.content, this.deleteDate, this.lastModification)
}