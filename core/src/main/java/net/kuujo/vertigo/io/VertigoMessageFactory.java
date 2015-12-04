package net.kuujo.vertigo.io;

import io.vertx.core.eventbus.Message;

/**
 * Factory for constructing network component instances.
 */
public interface VertigoMessageFactory {
    <T> VertigoMessage<T> createVertigoMessage(String id, Message<T> message);
}
