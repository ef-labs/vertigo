package net.kuujo.vertigo.message.impl;

import io.vertx.core.eventbus.Message;
import net.kuujo.vertigo.message.VertigoMessage;
import net.kuujo.vertigo.spi.VertigoMessageFactory;

/**
 * Default ComponentInstanceFactory.
 */
public class VertigoMessageFactoryImpl implements VertigoMessageFactory {

  @Override
  public <T> VertigoMessage<T> createVertigoMessage(String id, Message<T> message) {
    return new VertigoMessageImpl<T>(id, message);
  }

}
