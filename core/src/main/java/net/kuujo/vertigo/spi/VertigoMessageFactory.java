package net.kuujo.vertigo.spi;

import io.vertx.core.eventbus.Message;
import net.kuujo.vertigo.message.VertigoMessage;

/**
 * Factory for constructing network component instances.
 */
public interface VertigoMessageFactory {
    <T> VertigoMessage<T> createVertigoMessage(String id, Message<T> message);
}
