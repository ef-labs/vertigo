package net.kuujo.vertigo.instance.impl;

import io.vertx.core.Vertx;
import net.kuujo.vertigo.context.InputPortContext;
import net.kuujo.vertigo.instance.InputConnection;
import net.kuujo.vertigo.spi.VertigoMessageFactory;

public class ExternalInputConnection<T>  extends AbstractInputConnection<T> {

  public ExternalInputConnection(Vertx vertx, InputPortContext input, VertigoMessageFactory messageFactory) {
    super(vertx, null, messageFactory);
  }

  @Override
  public InputConnection<T> pause() {
    throw new UnsupportedOperationException();
  }

  @Override
  public InputConnection<T> resume() {
    throw new UnsupportedOperationException();
  }

}
