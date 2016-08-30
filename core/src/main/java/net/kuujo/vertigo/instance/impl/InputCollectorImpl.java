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

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.kuujo.vertigo.spi.ComponentInstanceFactory;
import net.kuujo.vertigo.instance.InputCollector;
import net.kuujo.vertigo.context.InputContext;
import net.kuujo.vertigo.instance.InputPort;
import net.kuujo.vertigo.context.InputPortContext;
import net.kuujo.vertigo.util.TaskRunner;

import java.util.*;

/**
 * Input collector implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class InputCollectorImpl implements InputCollector, Handler<Message<Object>> {
  private final Logger log;
  protected final Vertx vertx;
  protected InputContext context;
  protected final Map<String, InputPort> ports = new HashMap<>();
  private final TaskRunner tasks = new TaskRunner();
  private MessageConsumer<Object> consumer;

  public InputCollectorImpl(Vertx vertx, InputContext context, ComponentInstanceFactory factory) {
    this.vertx = vertx;
    this.context = context;
    this.log = LoggerFactory.getLogger(String.format("%s-%s", InputCollectorImpl.class.getName(), context.component().name()));
    init(factory);
  }

  /**
   * Initializes the output.
   * @param factory
   */
  private void init(ComponentInstanceFactory factory) {
    for (InputPortContext input : context.ports()) {
      if (!ports.containsKey(input.name())) {
        ports.put(input.name(), factory.createInputPort(vertx, input));
      }
    }
  }

  /**
   * Handles an input message.
   *
   * @param message The message to handle.
   */
  @Override
  @SuppressWarnings("unchecked")
  public void handle(Message<Object> message) {
    String portName = message.headers().get("port");
    if (portName != null) {
      InputPort port = ports.get(portName);
      if (port != null) {
        port.handle(message);
      }
    }
  }

  @Override
  public Collection<InputPort> ports() {
    List<InputPort> ports = new ArrayList<>(this.ports.size());
    for (InputPort port : this.ports.values()) {
      ports.add(port);
    }
    return ports;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> InputPort<T> port(String name) {
    return ports.get(name);
  }

  @Override
  public String toString() {
    return context.toString();
  }

}
