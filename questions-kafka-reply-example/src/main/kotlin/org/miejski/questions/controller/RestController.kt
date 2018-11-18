package org.miejski.questions.controller

import org.miejski.questions.domain.QuestionsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class RestController @Autowired constructor(val questionsService: QuestionsService) {

    @GetMapping("/{market}/questions/{id}")
    fun question(@PathVariable market: String, @PathVariable id: Int): QuestionStateDTO {
        return questionsService.get(market, id).toDto()
    }

    @GetMapping("/{market}/questions")
    fun allQuestions(@PathVariable market: String): List<QuestionStateDTO> { // TODO move to object
        return questionsService.getAll(market).map { it.toDto() }
    }

    @GetMapping("/{market}/questions/action/count")
    fun count(@PathVariable market: String): QuestionsInfoDTO {
        return QuestionsInfoDTO(questionsService.count())
    }
}

data class QuestionsInfoDTO(val count: Long)