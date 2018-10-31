package org.miejski.questions.source;

import java.util.concurrent.CompletableFuture;

public interface BusProducer {

    CompletableFuture<Boolean> start();

    void stop();
}
