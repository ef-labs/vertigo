package net.kuujo.vertigo.instance.impl;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.kuujo.vertigo.context.InputConnectionContext;
import net.kuujo.vertigo.instance.InputConnection;
import net.kuujo.vertigo.message.VertigoMessage;
import net.kuujo.vertigo.spi.VertigoMessageFactory;

import java.util.UUID;

/**
 * Created by Magnus.Koch on 8/30/2016.
 */
public abstract class AbstractInputConnection<T> implements InputConnection<T>, Handler<Message<T>> {

  private static final Logger logger = LoggerFactory.getLogger(AbstractInputConnection.class);

  protected static final String ACTION_HEADER = "action";
  protected static final String ID_HEADER = "name";
  protected static final String PAUSE_ACTION = "pause";
  protected static final String RESUME_ACTION = "resume";

  protected final Vertx vertx;
  protected final EventBus eventBus;
  protected final InputConnectionContext context;

  protected final VertigoMessageFactory messageFactory;
  protected Handler<VertigoMessage<T>> messageHandler;

  protected AbstractInputConnection(Vertx vertx, InputConnectionContext context, VertigoMessageFactory messageFactory) {
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
    this.context = context;
    this.messageFactory = messageFactory;
  }

  @Override
  public void handle(Message<T> message) {
    doMessage(message);
  }

  @Override
  public InputConnection<T> handler(Handler<VertigoMessage<T>> handler) {
    this.messageHandler = handler;
    return this;
  }

  /**
   * Handles receiving a message.
   * @param message the message to handle
   */
  @SuppressWarnings("unchecked")
  protected void doMessage(final Message<T> message) {
    if (messageHandler != null) {
      String id = message.headers().get(ID_HEADER);
      if (id == null) {
        id = UUID.randomUUID().toString();
      }
      VertigoMessage<T> vertigoMessage = messageFactory.<T>createVertigoMessage(id, message);
      logger.debug("{} - Received: Message[name={}, value={}]", this, id, message);
      doVertigoMessage(vertigoMessage);
    }
  }

  /**
   * Handles a Vertigo message (forwards to message handler).
   * @param vertigoMessage the message to handle
   */
  protected void doVertigoMessage(VertigoMessage<T> vertigoMessage) {
    messageHandler.handle(vertigoMessage);
  }

  @Override
  public String toString() {
    return context != null ? context.toString() : super.toString();
  }

}
