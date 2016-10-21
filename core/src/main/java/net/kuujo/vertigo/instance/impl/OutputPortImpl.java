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

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.kuujo.vertigo.spi.ComponentInstanceFactory;
import net.kuujo.vertigo.instance.ControllableOutput;
import net.kuujo.vertigo.instance.OutputConnection;
import net.kuujo.vertigo.context.OutputConnectionContext;
import net.kuujo.vertigo.instance.OutputPort;
import net.kuujo.vertigo.context.OutputPortContext;
import net.kuujo.vertigo.util.Args;
import net.kuujo.vertigo.util.CountingCompletionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Output port implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class OutputPortImpl<T> implements OutputPort<T>, ControllableOutput<OutputPort<T>, T>, Handler<Message<T>> {
  private static final Logger log = LoggerFactory.getLogger(OutputPortImpl.class);
  private static final int DEFAULT_SEND_QUEUE_MAX_SIZE = 10000;
  protected final Vertx vertx;
  protected OutputPortContext context;
  protected final Map<String, OutputConnection<T>> connections = new HashMap<>();
  private int maxQueueSize = DEFAULT_SEND_QUEUE_MAX_SIZE;
  private Handler<Void> drainHandler;

  public OutputPortImpl(Vertx vertx, OutputPortContext context, ComponentInstanceFactory factory) {
    this.vertx = vertx;
    this.context = context;
    init(factory);
  }

  /**
   * Initializes the output connections.
   * @param factory
   */
  private void init(ComponentInstanceFactory factory) {
    for (OutputConnectionContext connection : context.connections()) {
      connections.put(connection.target().address(), factory.<T>createOutputConnection(vertx, connection));
    }
  }

  @Override
  public void handle(Message<T> message) {
    String source = message.headers().get("source");
    if (source != null) {
      OutputConnection<T> connection = connections.get(source);
      if (connection != null) {
        connection.handle(message);
      }
    }
  }

  @Override
  public String name() {
    return context.name();
  }

  @Override
  public OutputPort<T> checkpoint() {
    return this;
  }

  @Override
  public OutputPort<T> replay() {
    return this;
  }

  @Override
  public OutputPort<T> setSendQueueMaxSize(int maxSize) {
    Args.checkPositive(maxSize, "max size must be a positive number");
    this.maxQueueSize = maxSize;
    for (OutputConnection<T> connection : connections.values()) {
      connection.setSendQueueMaxSize(maxQueueSize);
    }
    return this;
  }

  @Override
  public int getSendQueueMaxSize() {
    return maxQueueSize;
  }

  @Override
  public int size() {
    int highest = 0;
    for (OutputConnection<T> connection : connections.values()) {
      highest = Math.max(highest, connection.size());
    }
    return highest;
  }

  @Override
  public boolean sendQueueFull() {
    for (OutputConnection<T> stream : connections.values()) {
      if (stream.sendQueueFull()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public OutputPort<T> drainedHandler(Handler<Void> handler) {
    this.drainHandler = handler;
    for (OutputConnection<T> connection : connections.values()) {
      connection.drainedHandler(handler);
    }
    return this;
  }

  @Override
  public OutputPort<T> send(T message) {
    for (OutputConnection<T> connection : connections.values()) {
      connection.send(message);
    }
    return this;
  }

  @Override
  public OutputPort<T> send(T message, MultiMap headers) {
    for (OutputConnection<T> connection : connections.values()) {
      connection.send(message, headers);
    }
    return this;
  }

  @Override
  public OutputPort<T> send(T message, Handler<AsyncResult<Void>> ackHandler) {
    CountingCompletionHandler<Void> counter = new CountingCompletionHandler<>(connections.size());
    counter.setHandler(ackHandler);
    for (OutputConnection<T> connection : connections.values()) {
      connection.send(message, counter);
    }
    return this;
  }

  @Override
  public OutputPort<T> send(T message, MultiMap headers, Handler<AsyncResult<Void>> ackHandler) {
    CountingCompletionHandler<Void> counter = new CountingCompletionHandler<>(connections.size());
    counter.setHandler(ackHandler);
    for (OutputConnection<T> connection : connections.values()) {
      connection.send(message, headers, counter);
    }
    return this;
  }


  @Override
  public String toString() {
    return context.toString();
  }

}
