package org.miejski.questions.domain

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.miejski.questions.state.QuestionState
import org.miejski.questions.state.QuestionStateDe
import org.miejski.questions.state.QuestionStateSerde
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer


@Configuration
@EnableKafka
class KafkaConfig {

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, QuestionState> {
        val factory: ConcurrentKafkaListenerContainerFactory<String, QuestionState> = ConcurrentKafkaListenerContainerFactory()
        factory.setConsumerFactory(consumerFactory())
        factory.setConcurrency(1)
        factory.getContainerProperties().setPollTimeout(3000)
        return factory
    }

    @Bean
    fun consumerFactory(): ConsumerFactory<String, QuestionState> {
        return DefaultKafkaConsumerFactory<String, QuestionState>(consumerConfigs()) // TODO
    }

    @Bean
    fun consumerConfigs(): Map<String, Any> {
        val props = mutableMapOf<String, Any>()
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9093,localhost:9094")
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, QuestionStateDe::class.java)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        return props
    }
}