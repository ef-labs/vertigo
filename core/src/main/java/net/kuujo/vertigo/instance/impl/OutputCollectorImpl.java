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
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.kuujo.vertigo.spi.ComponentInstanceFactory;
import net.kuujo.vertigo.instance.OutputCollector;
import net.kuujo.vertigo.context.OutputContext;
import net.kuujo.vertigo.instance.OutputPort;
import net.kuujo.vertigo.context.OutputPortContext;

import java.util.*;

/**
 * Output collector implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class OutputCollectorImpl implements OutputCollector, Handler<Message<Object>> {

  private Logger logger;
  protected final Vertx vertx;
  protected OutputContext context;
  protected final Map<String, OutputPort> ports = new HashMap<>();
  private ComponentInstanceFactory factory;

  public OutputCollectorImpl(Vertx vertx, OutputContext context, ComponentInstanceFactory factory) {
    this.vertx = vertx;
    this.context = context;
    this.logger = LoggerFactory.getLogger(String.format("%s-%s", OutputCollectorImpl.class.getName(), context.component().name()));
    init(factory);
  }

  /**
   * Initializes the output.
   * @param factory
   */
  private void init(ComponentInstanceFactory factory) {
    this.factory = factory;
    for (OutputPortContext output : context.ports()) {
      if (!ports.containsKey(output.name())) {
        ports.put(output.name(), factory.createOutputPort(vertx, output));
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void handle(Message<Object> message) {
    String portName = message.headers().get("port");
    if (portName != null) {
      OutputPort port = ports.get(portName);
      if (port != null) {
        port.handle(message);
      }
    }
  }

  @Override
  public Collection<OutputPort> ports() {
    List<OutputPort> ports = new ArrayList<>(this.ports.size());
    for (OutputPort port : this.ports.values()) {
      ports.add(port);
    }
    return ports;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> OutputPort<T> port(String name) {
    if (!ports.containsKey(name)) {
      // Create stub port
      OutputPortContext portContext = OutputPortContext
          .builder()
          .setOutput(context)
          .setName(name)
          .build();
      ports.put(name, factory.createOutputPort(vertx, portContext));
      if (logger.isInfoEnabled()) {
        logger.info(
            String.format(
                "Dynamically created output port %s on component %s at address %s. The port has no connections.",
                name,
                context.component().name(),
                context.component().address()));
      }
    }
    return ports.get(name);
  }

  @Override
  public String toString() {
    return context.toString();
  }

}
