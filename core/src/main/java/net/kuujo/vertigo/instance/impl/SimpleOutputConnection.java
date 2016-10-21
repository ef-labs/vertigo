package net.kuujo.vertigo.instance.impl;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import net.kuujo.vertigo.context.OutputConnectionContext;
import net.kuujo.vertigo.instance.OutputConnection;

/**
 * Created by Magnus.Koch on 8/30/2016.
 */
public class SimpleOutputConnection<T> extends AbstractOutputConnection<T> {

  public SimpleOutputConnection(Vertx vertx, OutputConnectionContext context) {
    super(vertx, context);
  }

  @Override
  protected void doPause(long id) {
  }

  @Override
  protected void doResume(long id) {
  }

  @Override
  protected boolean isPaused() {
    return false;
  }

  @Override
  protected void doQueue(Payload payload) {
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public OutputConnection<T> setSendQueueMaxSize(int maxSize) {
    return this;
  }

  @Override
  public int getSendQueueMaxSize() {
    return Integer.MAX_VALUE;
  }

  @Override
  public boolean sendQueueFull() {
    return false;
  }

  @Override
  public OutputConnection<T> drainedHandler(Handler<Void> handler) {
    return null;
  }
}
