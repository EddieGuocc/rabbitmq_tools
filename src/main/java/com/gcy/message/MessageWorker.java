package com.gcy.message;

public interface MessageWorker<T extends Message> {
    void doWork(T message);
    Class<T> getMessageClass();
}
