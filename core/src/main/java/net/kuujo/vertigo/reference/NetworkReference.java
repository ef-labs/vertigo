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
package net.kuujo.vertigo.reference;

import io.vertx.codegen.annotations.VertxGen;

/**
 * NetworkConfig reference.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@VertxGen
public interface NetworkReference {

  /**
   * Returns a reference to a component in the network.
   *
   * @param id The unique ID of the component to reference.
   * @return The component reference.
   */
  ComponentReference component(String id);

  /**
   * Returns an output port that receives all messages connected from components to the network.
   * If multiple components are connected, all messages will be received.
   * @param <T>
   * @return
   */
  <T> NetworkOutputReference<T> output();

  /**
   * Returns an input port that can be used to send messages to components that are connected to the network.
   * If multiple components are connected, one message will be sent per connection.
   * @param <T>
   * @return
   */
  <T> NetworkInputReference<T> input();

}
