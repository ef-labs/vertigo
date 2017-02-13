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
package net.kuujo.vertigo.reference.impl;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.kuujo.vertigo.context.InputContext;
import net.kuujo.vertigo.context.PortContext;
import net.kuujo.vertigo.reference.InputPortReference;
import net.kuujo.vertigo.reference.InputReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Input reference implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class InputReferenceImpl implements InputReference {

  private static final Logger logger = LoggerFactory.getLogger(InputReferenceImpl.class);

  private final Vertx vertx;
  private final String address;
  private final Map<String, InputPortReference> ports;

  public InputReferenceImpl(Vertx vertx, String address, InputContext input) {
    this.vertx = vertx;
    this.address = address;
    this.ports = input.ports()
        .stream()
        .collect(Collectors
            .toConcurrentMap(
                PortContext::name,
                port -> new InputPortReferenceImpl(vertx, address, port.name())));
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> InputPortReference<T> port(String name) {
    InputPortReference port = ports.get(name);
    if (port == null) {
      port = new InputPortReferenceImpl<>(vertx, address, name);
      ports.put(name, port);
      logger.info(
          "Dynamically created input port {} at address {}. The port has no connections.",
          name,
          address);
    }
    return port;
  }

  @Override
  public List<InputPortReference> ports() {
    return new ArrayList<>(ports.values());
  }

}
