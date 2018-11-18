package org.miejski.questions.domain

import org.springframework.context.annotation.Bean
import org.springframework.kafka.listener.KafkaListenerErrorHandler
import org.springframework.kafka.listener.ListenerExecutionFailedException
import org.springframework.messaging.Message
import org.springframework.stereotype.Component

@Component(value = "org.miejski.questions.domain.ConsumerError")
class ConsumerError : KafkaListenerErrorHandler {
    override fun handleError(message: Message<*>?, exception: ListenerExecutionFailedException?): Any {
        println("saddsa")
        return "OK"
    }
}