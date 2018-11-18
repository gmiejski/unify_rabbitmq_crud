package org.miejski.questions;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.miejski.TopologyBuilder;
import org.miejski.questions.events.QuestionCreated;
import org.miejski.questions.events.QuestionDeleted;
import org.miejski.questions.events.QuestionUpdated;
import org.miejski.questions.state.QuestionState;
import org.miejski.questions.state.QuestionStateSerde;

import java.util.HashMap;

import static org.miejski.questions.QuestionsStateTopology.READ_ONLY_STATE_TOPIC;

public class QuestionsStateReaderTopology implements TopologyBuilder {

    @Override
    public Topology buildTopology(StreamsBuilder streamsBuilder) {
        KStream<String, QuestionState> allQuestionStates = streamsBuilder.stream(READ_ONLY_STATE_TOPIC, Consumed.with(Serdes.String(), QuestionStateSerde.questionStateSerde()));
        allQuestionStates.groupByKey().reduce(this::chooseNewerQuestionState, Materialized.<String, QuestionState, KeyValueStore<Bytes, byte[]>>as(QuestionsStateTopology.QUESTIONS_RO_STORE_NAME).withKeySerde(Serdes.String()).withValueSerde(QuestionStateSerde.questionStateSerde()));
        return streamsBuilder.build();
    }

    private QuestionState chooseNewerQuestionState(QuestionState value1, QuestionState value2) {
        if (value1.getLastModification().isAfter(value2.getLastModification())) {
            return value1;
        } else if (value2.getLastModification().isAfter(value1.getLastModification())) {
            return value2;
        }
        return value1;
    }
}
