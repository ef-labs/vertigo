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

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.test.core.VertxTestBase;
import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.component.MessageHandlerComponent;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.message.VertigoMessage;
import net.kuujo.vertigo.reference.NetworkReference;
import net.kuujo.vertigo.util.CountingCompletionHandler;

import java.util.concurrent.CountDownLatch;

/**
 * Provides simplified base classes for simple network integration tests.
 */
public abstract class VertigoTestBase extends VertxTestBase {

  private static Logger logger = LoggerFactory.getLogger(VertigoTestBase.class.getName());
  protected static Void VOID = null;
  private NetworkReference networkReference;

  protected abstract NetworkConfig createNetwork();

  @Override
  public void setUp() throws Exception {
    super.setUp();

    NetworkConfig network = createNetwork();

    // Deploy network
    CountDownLatch latch = new CountDownLatch(1);

    vertx.runOnContext(aVoid -> {
      Vertigo vertigo = Vertigo.vertigo(vertx);
      vertigo.deployNetwork(network, result -> {
        if (result.failed()) {
          fail(result.cause().getMessage());
        }
        else {
          this.networkReference = result.result();
        }
        latch.countDown();
      });
    });

    latch.await();

  }

  protected static Logger logger() {
    return logger;
  }

  public NetworkReference getNetworkReference() {
    return networkReference;
  }

  public static class AutoAckingComponent extends MessageHandlerComponent<Object> {
    @Override
    public void handle(VertigoMessage<Object> event) {
      logger().info(context().name() + " received message " + event.body() + ", acking.");
      event.ack();
    }
  }

  public static class AutoForwardingComponent extends MessageHandlerComponent<String> {

    @Override
    public void handle(VertigoMessage<String> message) {

      logger().info(context().name() + " received message " + message.body());

      // Transform message
      String trace = message.body() + " > " + context().name();

      if (output().ports().size() > 0) {

        CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(output().ports().size())
            .setHandler(message::handle);

        output()
            .ports()
            .forEach(outputPort -> {
              output()
                  .port(outputPort.name())
                  .send(trace, counter);
            });
      }
      else {
        message.ack();

      }

    }
  }
}