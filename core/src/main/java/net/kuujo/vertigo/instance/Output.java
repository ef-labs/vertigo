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
package net.kuujo.vertigo.instance;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;

/**
 * Output interface.<p>
 *
 * This is the base interface for all output interfaces. It exposes basic
 * methods for grouping and sending messages.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface Output<T extends Output<T, U>, U> {

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @return The output partition.
   */
  T send(U message);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @param headers The message headers.
   * @return The output partition.
   */
  T send(U message, MultiMap headers);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @param ackHandler An asynchronous handler to be called when the message is acked.
   * @return The output partition.
   */
  <V> T send(U message, Handler<AsyncResult<V>> ackHandler);

  /**
   * Sends a message on the output.
   *
   * @param message The message to send.
   * @param headers The message headers.
   * @param ackHandler An asynchronous handler to be called when the message is acked.
   * @return The output partition.
   */
  <V> T send(U message, MultiMap headers, Handler<AsyncResult<V>> ackHandler);

}
