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

package net.kuujo.vertigo.message.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import net.kuujo.vertigo.message.VertigoMessage;

/**
 * Vertigo message implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class VertigoMessageImpl<T> implements VertigoMessage<T> {
  private Message<T> message;
  private String id;
  private T body;
  private MultiMap headers;
  private boolean acked;

  public VertigoMessageImpl(String id, Message<T> message) {
    this.id = id;
    this.body = message.body();
    this.headers = message.headers();
    this.message = message;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public T body() {
    return body;
  }

  @Override
  public MultiMap headers() {
    return headers;
  }

  @Override
  public void ack(Object reply) {
    if (!acked) {
      acked = true;
      message.reply(reply);
    }
  }

  @Override
  public boolean acked() {
    return acked;
  }

  @Override
  public void fail(Throwable cause) {
    if (!acked) {
      if (cause instanceof ReplyException) {
        ReplyException exception = (ReplyException) cause;
        message.fail(exception.failureCode(), exception.getMessage());
      }
      else if (cause != null) {
        message.fail(-1, cause.getClass().getSimpleName() + ": " + cause.getMessage());
//        logger.error(cause.getMessage(), cause);
      }
      else {
        message.fail(-1, "Unknown error.");
//        logger.error(new VertigoException("Unknown error."));
      }
      acked = true;
    }
  }

  @Override
  public <V> void handle(AsyncResult<V> result) {
    if (result.succeeded()) {
      ack(result.result());
    } else {
      fail(result.cause());
    }
  }

}

