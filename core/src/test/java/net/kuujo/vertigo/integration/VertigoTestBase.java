package net.kuujo.vertigo.integration;

/*
 * Copyright 2013-2014 the original author or authors.
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

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.test.core.VertxTestBase;
import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.component.AbstractComponent;
import net.kuujo.vertigo.io.VertigoMessage;
import net.kuujo.vertigo.network.Network;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * Provides simplified base classes for simple network integration tests.
 */
public abstract class VertigoTestBase extends VertxTestBase {

  static String startAddress = UUID.randomUUID().toString();
  private static Logger logger = LoggerFactory.getLogger(VertigoTestBase.class.getName());
  protected static Void VOID = null;

  protected abstract Network createNetwork();

  @Override
  public void setUp() throws Exception {
    super.setUp();

    Network network = createNetwork();

    // Deploy network
    CountDownLatch latch = new CountDownLatch(1);

    vertx.runOnContext(aVoid -> {
      Vertigo vertigo = Vertigo.vertigo(vertx);
      vertigo.deployNetwork(network, result -> {
        if (result.failed()) {
          fail(result.cause().getMessage());
        }
        latch.countDown();
      });
    });

    latch.await();

  }

  @Test
  public void test() throws Exception {
    sendStartSignal();
    await();
  }

  protected void sendStartSignal() {

    // Send start event
    vertx.eventBus().send(startAddress, VOID, r -> {
          try {
            startEventComplete(r);
          } catch (Throwable e) {
            fail(e.getMessage());
          }
        }
    );
  }

  protected void startEventComplete(AsyncResult<Message<Object>> result) throws Throwable {
    if (result.succeeded()) {
      logger().info("Test complete: " + this.getClass().getSimpleName());
      testComplete();
    }
    else {
      logger().info("Test failed: " + this.getClass().getSimpleName());
      fail(result.cause().getMessage());
    }
  }

  protected static Logger logger() {
    return logger;
  }

  public abstract static class StartComponentBase extends AbstractComponent implements Handler<Message<Void>> {

    @Override
    public void start() throws Exception {
      vertx.eventBus().consumer(startAddress, this);
    }

    @Override
    public void handle(Message<Void> event) {
      logger().info(component().context().name() + " received start event.");
      testStart(r -> {
        if (r.succeeded()) {
          event.reply(VOID);
        } else {
          event.fail(-1, r.cause().getMessage());
        }
      });
    }

    protected abstract void testStart(Handler<AsyncResult<Void>> completeHandler);

  }

  @SuppressWarnings("unchecked")
  public abstract static class InputComponentBase<M> extends AbstractComponent implements Handler<VertigoMessage<M>> {

    @Override
    public void start() throws Exception {
      component()
          .input()
          .ports()
          .forEach(port -> port.handler(this));
    }

  }

  public static class AutoAckingComponent extends InputComponentBase<Object> {
    @Override
    public void handle(VertigoMessage<Object> event) {
      logger().info(component().context().name() + " received message " + event.body() + ", acking.");
      event.ack();
    }
  }

}