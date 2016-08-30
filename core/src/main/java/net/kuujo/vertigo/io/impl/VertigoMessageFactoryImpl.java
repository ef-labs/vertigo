package net.kuujo.vertigo.io.impl;

import io.vertx.core.eventbus.Message;
import net.kuujo.vertigo.io.VertigoMessage;
import net.kuujo.vertigo.io.VertigoMessageFactory;

/**
 * Default ComponentInstanceFactory.
 */
public class VertigoMessageFactoryImpl implements VertigoMessageFactory {

  @Override
  public <T> VertigoMessage<T> createVertigoMessage(String id, Message<T> message) {
    return new VertigoMessageImpl<T>(id, message);
  }

}
