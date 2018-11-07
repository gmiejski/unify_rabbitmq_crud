package org.miejski.questions.bus2kafka;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.miejski.TopologyBuilder;
import org.miejski.questions.QuestionID;
import org.miejski.questions.QuestionsStateTopology;
import org.miejski.questions.events.QuestionCreated;
import org.miejski.questions.events.QuestionDeleted;
import org.miejski.questions.events.QuestionModifier;
import org.miejski.questions.events.QuestionUpdated;
import org.miejski.questions.source.create.SourceQuestionCreated;
import org.miejski.questions.source.delete.SourceQuestionDeleted;
import org.miejski.questions.source.update.SourceQuestionUpdated;
import org.miejski.simple.objects.serdes.JSONSerde;

import java.util.function.Function;

public class Bus2KafkaMappingTopology implements TopologyBuilder {

    public static final String CREATE_TOPIC = "rabbit_create_topic";
    public static final String UPDATE_TOPIC = "rabbit_update_topic";
    public static final String DELETE_TOPIC = "rabbit_delete_topic";

    @Override
    public Topology buildTopology(StreamsBuilder streamsBuilder) {

        forwardToTopic(streamsBuilder, CREATE_TOPIC, QuestionsStateTopology.CREATE_TOPIC, sourceCreateMapper(), SourceQuestionCreated.class);
        forwardToTopic(streamsBuilder, UPDATE_TOPIC, QuestionsStateTopology.UPDATE_TOPIC, sourceUpdateMapper(), SourceQuestionUpdated.class);
        forwardToTopic(streamsBuilder, DELETE_TOPIC, QuestionsStateTopology.DELETE_TOPIC, sourceDeleteMapper(), SourceQuestionDeleted.class);

        return streamsBuilder.build();
    }

    private Function<QuestionModifier, QuestionModifier> sourceCreateMapper() {
        return questionModifier -> {
            SourceQuestionCreated source = (SourceQuestionCreated) questionModifier;
            return new QuestionCreated(source.getMarket(), QuestionID.from(source.ID()).getQuestionID(), source.getPayload().getContent(), source.getPayload().getCreateDate());
        };
    }

    private Function<QuestionModifier, QuestionModifier> sourceUpdateMapper() {
        return questionModifier -> {
            SourceQuestionUpdated source = (SourceQuestionUpdated) questionModifier;
            return new QuestionUpdated(source.getMarket(), QuestionID.from(source.ID()).getQuestionID(), source.getPayload().getContent(), source.getPayload().getEditedAt());
        };
    }

    private Function<QuestionModifier, QuestionModifier> sourceDeleteMapper() {
        return questionModifier -> {
            SourceQuestionDeleted source = (SourceQuestionDeleted) questionModifier;
            return new QuestionDeleted(source.getMarket(), QuestionID.from(source.ID()).getQuestionID(), source.getPayload().getDeletedAt());
        };
    }

    private void forwardToTopic(
            StreamsBuilder streamsBuilder,
            String inputTopic,
            String outputTopic,
            Function<QuestionModifier, QuestionModifier> mapper,
            Class<? extends QuestionModifier> sourceClass
    ) {
        Consumed<String, QuestionModifier> sourceTopicConsume = Consumed.with(Serdes.String(), JSONSerde.questionsModifierSerde(sourceClass));
        KStream<String, QuestionModifier> stringQuestionModifierKStream = streamsBuilder.stream(inputTopic, sourceTopicConsume)
                .selectKey((key, value) -> value.ID())
                .mapValues(mapper::apply);

        stringQuestionModifierKStream
                .to(outputTopic, Produced.with(Serdes.String(), JSONSerde.questionsModifierSerde(SourceQuestionCreated.class)));

    }
}
