package org.miejski.questions.source;

import java.util.List;
import java.util.function.Function;

public interface SourceEventProducer<T> {
    T create();
}
