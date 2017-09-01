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
package net.kuujo.vertigo.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * An incremental version of the counting completion handler.
 *
 * @author <a href="http://github.com/ef-labs">Magnus Koch</a>
 */
public class IncrementalCompletionHandler<T> {
  private int count;
  private int required;
  private Handler<AsyncResult<T>> doneHandler;
  private Throwable cause;
  private boolean failed;
  private boolean complete;
  private T result;

  public IncrementalCompletionHandler() {
  }

  /**
   * Increases the required operation count and returns a completion handler for that operation.
   */
  public Handler<AsyncResult<T>> increment() {
    if (complete) {
      throw new IllegalStateException("Cannot increment after completed() has been called.");
    }
    required++;
    return event -> {
      if (event.succeeded()) {
        // Keep the last non-null result
        if (event.result() != null) {
          this.result = event.result();
        }
        count++;
        checkDone();
      } else {
        fail(event.cause());
      }
    };
  }

  /**
   * Signals that all operations are registered
   *
   * @param handler
   */
  public void completed(Handler<AsyncResult<T>> handler) {
    setHandler(handler);
    this.complete = true;
    checkDone();
  }

  /**
   * Indicates that a call failed. This will immediately fail the handler.
   *
   * @param t The cause of the failure.
   */
  protected void fail(Throwable t) {
    if (!failed) {
      failed = true;
      cause = t;
      checkDone();
    }
  }

  /**
   * Sets the completion handler.
   *
   * @param doneHandler An asynchronous handler to be called once all calls have completed.
   */
  protected IncrementalCompletionHandler setHandler(Handler<AsyncResult<T>> doneHandler) {
    this.doneHandler = doneHandler;
    checkDone();
    return this;
  }

  /**
   * Checks whether the handler should be called.
   */
  protected void checkDone() {
    if (failed && doneHandler != null) {
      doneHandler.handle(Future.failedFuture(cause));
      doneHandler = null;
    } else if (complete && count == required) {
      doneHandler.handle(Future.succeededFuture(result));
      doneHandler = null;
    }
  }

}
