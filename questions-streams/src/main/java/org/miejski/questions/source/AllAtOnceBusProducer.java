package org.miejski.questions.source;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class AllAtOnceBusProducer<T> implements BusProducer {

    private final String market;
    private List<T> events;
    private final Consumer<Object> consumer;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private Thread runningThread;
    private CompletableFuture<Boolean> completed;
    private final Object x;

    public AllAtOnceBusProducer(String market, List<T> events, Consumer<Object> consumer) {
        this.market = market;
        this.events = events;
        this.consumer = consumer;
        this.x = new Object();
    }

    public CompletableFuture<Boolean> start() {
        synchronized (x) {
            completed = new CompletableFuture<>();
        }

        runningThread = new Thread(() -> {
            for (int i = 0; i < events.size(); i++) {
                if (i % 1000 == 0 && !this.isRunning.get()) {
                    break;
                }
                try {
                    consumer.accept(events.get(i));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            synchronized (x) {
                completed.complete(true);
            }
        });
        runningThread.start();
        return completed;
    }

    public void stop() {
        this.isRunning.set(false);
        while (runningThread.isAlive()) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        synchronized (x) {
            completed.complete(true);
        }
    }
}
