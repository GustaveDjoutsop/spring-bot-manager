package com.botmanager.core.queue;

import com.botmanager.config.QueueProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

@Slf4j
@Component
public class QueueManager {

    private final BlockingQueue<MessageJob> queue;

    private final int maxSize;

    private Consumer<MessageJob> processor;

    private volatile boolean running = false;

    private Thread processorThread;

    public QueueManager(QueueProperties queueProperties) {
        this.maxSize = queueProperties.getMaxSize();
        this.queue = new LinkedBlockingQueue<>(maxSize);
    }

    public boolean enqueue(MessageJob job) {
        if (queue.size() >= maxSize) {
            log.warn("Queue is full, dropping message from {}", job.getFrom());

            return false;
        }

        boolean added = queue.offer(job);
        if (added) {
            log.debug("Enqueued message from {} (queue size: {})", job.getFrom(), queue.size());
        }

        return added;
    }

    public void setProcessor(Consumer<MessageJob> processor) {
        this.processor = processor;

        if (!running) {
            startProcessing();
        }
    }

    private void startProcessing() {
        running = true;
        processorThread = new Thread(() -> {
            while (running) {
                try {
                    MessageJob job = queue.take();

                    if (processor != null) {
                        try {
                            processor.accept(job);
                        } catch (Exception exception) {
                            log.error("Error processing message from {}: {}", job.getFrom(), exception.getMessage());
                        }
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "queue-processor");
        processorThread.setDaemon(true);
        processorThread.start();

        log.info("Queue processor started");
    }

    public void stop() {
        running = false;

        if (processorThread != null) {
            processorThread.interrupt();
        }
    }

    public int size() {
        return queue.size();
    }

}
