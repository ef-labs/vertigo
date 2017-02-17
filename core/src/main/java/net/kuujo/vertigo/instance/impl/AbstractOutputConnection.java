package net.kuujo.vertigo.instance.impl;

import io.vertx.core.*;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.kuujo.vertigo.context.OutputConnectionContext;
import net.kuujo.vertigo.instance.OutputConnection;

import java.util.UUID;

/**
 * Created by Magnus.Koch on 8/30/2016.
 */
public abstract class AbstractOutputConnection<T> implements OutputConnection<T>, Handler<Message<T>> {
  protected static final String ACTION_HEADER = "action";
  protected static final String PORT_HEADER = "port";
  protected static final String TARGET_HEADER = "target";
  protected static final String ID_HEADER = "name";
  protected static final String INDEX_HEADER = "index";
  protected static final String MESSAGE_ACTION = "message";
  protected static final String PAUSE_ACTION = "pause";
  protected static final String RESUME_ACTION = "resume";

  protected final Vertx vertx;
  protected final EventBus eventBus;
  protected final OutputConnectionContext context;

  private static final Logger logger = LoggerFactory.getLogger(AbstractOutputConnection.class);

  public AbstractOutputConnection(Vertx vertx, OutputConnectionContext context) {
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
    this.context = context;
  }

  @Override
  public void handle(Message<T> message) {
    String action = message.headers().get(ACTION_HEADER);
    Long id = Long.valueOf(message.headers().get(INDEX_HEADER));
    switch (action) {
      case PAUSE_ACTION:
        doPause(id);
        break;
      case RESUME_ACTION:
        doResume(id);
        break;
    }
  }

  /**
   * Handles a connection pause.
   * @param id the id to pause
   */
  protected abstract void doPause(long id);

  /**
   * Handles a connection resume.
   * @param id the id to resume
   */
  protected abstract void doResume(long id);

  protected abstract boolean isPaused();

  /**
   * Sends a message.
   *
   * @param message the message to send
   * @param headers the headers to add to the request
   * @param ackHandler a handler for the request response
   * @return a reference to this object
   */
  protected OutputConnection<T> trySend(Object message, MultiMap headers, Handler<AsyncResult<Void>> ackHandler) {
    Payload payload = createPayload(message, headers, ackHandler);
    if (!isPaused()) {
      doSend(payload);
    }
    else {
      doQueue(payload);
    }
    return this;
  }

  protected abstract void doQueue(Payload payload);

  protected OutputConnection<T> doSend(Payload payload) {
    if (logger.isDebugEnabled()) {
      logger.debug("{} - Send: Message[name={}, message={}]", this, payload.getId(), payload.getMessage());
    }

    if (payload.getAckHandler() != null) {
      eventBus.send(context.target().address(), payload.getMessage(), payload.getOptions(), r -> {
        if (r.succeeded()) {
          payload.getAckHandler().handle(Future.succeededFuture());
        } else {
          payload.getAckHandler().handle(Future.failedFuture(r.cause()));
        }
      });
    } else {
      eventBus.send(context.target().address(), payload.getMessage(), payload.getOptions());
    }

    return this;
  }

  protected Payload createPayload(Object message, MultiMap headers, Handler<AsyncResult<Void>> ackHandler) {
    String id = UUID.randomUUID().toString();

    // Set up the message headers.
    DeliveryOptions options = new DeliveryOptions();
    if (headers == null) {
      headers = new CaseInsensitiveHeaders();
    }
    headers.add(ACTION_HEADER, MESSAGE_ACTION)
        .add(ID_HEADER, id)
        .add(PORT_HEADER, context.target().port())
        .add(TARGET_HEADER, context.target().address());

    options.setHeaders(headers);
    if (context.sendTimeout() > 0) {
      options.setSendTimeout(context.sendTimeout());
    }

    return new Payload()
        .setMessage(message)
        .setId(id)
        .setHeaders(headers)
        .setOptions(options)
        .setAckHandler(ackHandler);
  }

  @Override
  public OutputConnection<T> send(T message) {
    return trySend(message, null, null);
  }

  @Override
  public OutputConnection<T> send(T message, MultiMap headers) {
    return trySend(message, headers, null);
  }

  @Override
  public OutputConnection<T> send(T message, Handler<AsyncResult<Void>> ackHandler) {
    return trySend(message, null, ackHandler);
  }

  @Override
  public OutputConnection<T> send(T message, MultiMap headers, Handler<AsyncResult<Void>> ackHandler) {
    return trySend(message, headers, ackHandler);
  }

  @Override
  public String toString() {
    return context.toString();
  }

  protected static class Payload {

    private String id;
    private MultiMap headers;
    private DeliveryOptions options;
    private Object message;
    private Handler<AsyncResult<Void>> ackHandler;

    public Payload setId(String id) {
      this.id = id;
      return this;
    }

    public Payload setHeaders(MultiMap headers) {
      this.headers = headers;
      return this;
    }

    public Payload setOptions(DeliveryOptions options) {
      this.options = options;
      return this;
    }

    public String getId() {
      return id;
    }

    public MultiMap getHeaders() {
      return headers;
    }

    public DeliveryOptions getOptions() {
      return options;
    }

    public Object getMessage() {
      return message;
    }

    public Handler<AsyncResult<Void>> getAckHandler() {
      return ackHandler;
    }

    public Payload setMessage(Object message) {
      this.message = message;
      return this;
    }

    public Payload setAckHandler(Handler<AsyncResult<Void>> ackHandler) {
      this.ackHandler = ackHandler;
      return this;
    }
  }

}
