package org.miejski.questions

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RestQuestionState

fun main(args: Array<String>) {
    runApplication<RestQuestionState>(*args)
}