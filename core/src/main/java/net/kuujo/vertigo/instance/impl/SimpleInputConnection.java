package net.kuujo.vertigo.instance.impl;

import io.vertx.core.Vertx;
import net.kuujo.vertigo.context.InputConnectionContext;
import net.kuujo.vertigo.instance.InputConnection;
import net.kuujo.vertigo.spi.VertigoMessageFactory;

/**
 * Created by Magnus.Koch on 8/30/2016.
 */
public class SimpleInputConnection<T> extends AbstractInputConnection<T> {

  protected SimpleInputConnection(Vertx vertx, InputConnectionContext context, VertigoMessageFactory messageFactory) {
    super(vertx, context, messageFactory);
  }

  @Override
  public InputConnection<T> pause() {
    return this;
  }

  @Override
  public InputConnection<T> resume() {
    return this;
  }

}
