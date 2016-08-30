/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kuujo.vertigo.instance.impl;

import io.vertx.core.*;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.kuujo.vertigo.instance.OutputConnection;
import net.kuujo.vertigo.context.OutputConnectionContext;

import java.util.TreeMap;
import java.util.UUID;

/**
 * Default output connection implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class OutputConnectionImpl<T> implements OutputConnection<T>, Handler<Message<T>> {
  protected static final String ACTION_HEADER = "action";
  protected static final String PORT_HEADER = "port";
  protected static final String SOURCE_HEADER = "source";
  protected static final String ID_HEADER = "name";
  protected static final String INDEX_HEADER = "index";
  protected static final String MESSAGE_ACTION = "message";
  protected static final String ACK_ACTION = "ack";
  protected static final String FAIL_ACTION = "fail";
  protected static final String PAUSE_ACTION = "pause";
  protected static final String RESUME_ACTION = "resume";
  protected static final int DEFAULT_MAX_QUEUE_SIZE = 1000;
  private final Logger log;
  protected final Vertx vertx;
  protected final EventBus eventBus;
  protected final OutputConnectionContext context;
  private int maxQueueSize = DEFAULT_MAX_QUEUE_SIZE;
  private Handler<Void> drainHandler;
  private long currentMessage = 1;
  private final TreeMap<Long, JsonObject> messages = new TreeMap<>();
  //protected final TreeMap<Long, Handler<AsyncResult<Void>>> ackHandlers = new TreeMap<>();
  private boolean full;
  private boolean paused;

  public OutputConnectionImpl(Vertx vertx, OutputConnectionContext context) {
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
    this.context = context;
    this.log = LoggerFactory.getLogger(String.format("%s-%s", OutputConnectionImpl.class.getName(), context.port().output().component().address()));
  }

  @Override
  public void handle(Message<T> message) {
    String action = message.headers().get(ACTION_HEADER);
    Long id = Long.valueOf(message.headers().get(INDEX_HEADER));
    switch (action) {
      case ACK_ACTION:
        doAck(id);
        break;
      case FAIL_ACTION:
        doFail(id);
        break;
      case PAUSE_ACTION:
        doPause(id);
        break;
      case RESUME_ACTION:
        doResume(id);
        break;
    }
  }

  @Override
  public OutputConnection<T> setSendQueueMaxSize(int maxSize) {
    this.maxQueueSize = maxSize;
    return this;
  }

  @Override
  public int getSendQueueMaxSize() {
    return maxQueueSize;
  }

  @Override
  public int size() {
    return messages.size();
  }

  @Override
  public boolean sendQueueFull() {
    return paused || messages.size() >= maxQueueSize;
  }

  @Override
  public OutputConnection<T> drainHandler(Handler<Void> handler) {
    this.drainHandler = handler;
    return this;
  }

  /**
   * Checks whether the connection is full.
   */
  protected void checkFull() {
    if (!full && messages.size() >= maxQueueSize) {
      full = true;
      log.debug(String.format("%s - Connection to %s is full", this, context.target()));
    }
  }

  /**
   * Checks whether the connection has been drained.
   */
  protected void checkDrain() {
    if (full && !paused && messages.size() < maxQueueSize / 2) {
      full = false;
      log.debug(String.format("%s - Connection to %s is drained", this, context.target()));
      if (drainHandler != null) {
        drainHandler.handle((Void)null);
      }
    }
  }

  /**
   * Handles a batch ack.
   */
  protected void doAck(long id) {
    // The other side of the connection has sent a message indicating which
    // messages it has seen. We can clear any messages before the indicated ID.
    if (log.isDebugEnabled()) {
      log.debug(String.format("%s - Received ack for messages up to %d, removing all previous messages from memory", this, id));
    }
    if (messages.containsKey(id)) {
      messages.headMap(id, true).clear();
    } else {
      messages.clear();
    }
    checkDrain();
  }

  /**
   * Handles a batch fail.
   */
  protected void doFail(long id) {
    if (log.isDebugEnabled()) {
      log.debug(String.format("%s - Received resend request for messages starting at %d", this, id));
    }

    // Ack all the entries before the given ID.
    doAck(id);

    // Now that all the entries before the given ID have been removed,
    // just iterate over the messages map and resend all the messages.
    for (JsonObject message : messages.values()) {
      eventBus.send(context.target().address(), message);
    }
  }

  /**
   * Handles a connection pause.
   */
  protected void doPause(long id) {
    log.debug(String.format("%s - Paused connection to %s", this, context.target()));
    paused = true;
  }

  /**
   * Handles a connection resume.
   */
  protected void doResume(long id) {
    if (paused) {
      log.debug(String.format("%s - Resumed connection to %s", this, context.target()));
      paused = false;
      checkDrain();
    }
  }

  /**
   * Sends a message.
   */
  protected OutputConnection<T> doSend(Object message, MultiMap headers, Handler<AsyncResult<Void>> ackHandler) {
    if (!paused) {
      // Generate a unique ID and monotonically increasing index for the message.
      String id = UUID.randomUUID().toString();
      long index = currentMessage++;

      /*
      Commented out tracking of ackHandlers. Presumably these need to be kept around so new handlers can be created
      if messages are resent.
      // if (ackHandler != null) {
      //   ackHandlers.put(index, ackHandler);
      // }
      */

      // Set up the message headers.
      DeliveryOptions options = new DeliveryOptions();
      if (headers == null) {
        headers = new CaseInsensitiveHeaders();
      }
      headers.add(ACTION_HEADER, MESSAGE_ACTION)
          .add(ID_HEADER, id)
          .add(INDEX_HEADER, String.valueOf(index))
          .add(PORT_HEADER, context.target().port())
          .add(SOURCE_HEADER, context.target().address()) // TODO: header is called source, but takes the address...
          .add(ID_HEADER, id)
          .add(INDEX_HEADER, String.valueOf(index));

      options.setHeaders(headers);
      if (context.sendTimeout() > 0) {
        options.setSendTimeout(context.sendTimeout());
      }

      if (log.isDebugEnabled()) {
        log.debug(String.format("%s - Send: Message[name=%s, message=%s]", this, id, message));
      }

      if (ackHandler != null) {
        eventBus.send(context.target().address(), message, options, r -> {
          if (r.succeeded()) {
            ackHandler.handle(Future.<Void>succeededFuture());
          } else {
            ackHandler.handle(Future.<Void>failedFuture(r.cause()));
          }
        });
      } else {
        eventBus.send(context.target().address(), message, options);
      }
      checkFull();
    }
    return this;
  }

  @Override
  public OutputConnection<T> send(T message) {
    return doSend(message, null, null);
  }

  @Override
  public OutputConnection<T> send(T message, MultiMap headers) {
    return doSend(message, headers, null);
  }

  @Override
  public OutputConnection<T> send(T message, Handler<AsyncResult<Void>> ackHandler) {
    return doSend(message, null, ackHandler);
  }

  @Override
  public OutputConnection<T> send(T message, MultiMap headers, Handler<AsyncResult<Void>> ackHandler) {
    return doSend(message, headers, ackHandler);
  }

  @Override
  public String toString() {
    return context.toString();
  }

}
