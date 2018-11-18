package org.miejski.questions

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories
class RestQuestionState

fun main(args: Array<String>) {
    runApplication<RestQuestionState>(*args)
}