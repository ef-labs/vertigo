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

import net.kuujo.vertigo.network.builder.NetworkBuilder;
import net.kuujo.vertigo.component.MessageHandlerComponent;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.message.VertigoMessage;
import net.kuujo.vertigo.reference.NetworkReference;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Forward_Timeout_Test extends VertigoTestBase {
  static CompletableFuture<Void> targetReceived;

  @Override
  protected NetworkConfig createNetwork() {
    NetworkBuilder builder = NetworkConfig.builder();

    builder.component("A")
        .identifier(ForwardingComponent.class.getName())
        .input().port("in");

    builder.component("B")
        .identifier(ForwardingComponent.class.getName());

    builder.component("C")
        .identifier(TargetComponent.class.getName());

    builder
        .connect("A").port("out")
        .to("B").port("in");

    builder
        .connect("B").port("out")
        .to("C").port("in")
        .sendTimeout(1);

    return builder.build();
  }

  @Test
  public void timeoutTest() throws InterruptedException {
    NetworkReference network = getNetworkReference();
    targetReceived = new CompletableFuture<>();
    CompletableFuture<Void> sendCompleted = new CompletableFuture<>();

    network
        .component("B").input().port("in")
        .send("Word", message -> {
          assertTrue(message.failed());
          logger().info("Send failed: " + message.cause().getMessage());
          sendCompleted.complete(null);
          assertFalse(targetReceived.isDone());
        });

    CompletableFuture.allOf(sendCompleted, targetReceived).join();
    testComplete();

  }

  @Test
  public void chainedTimeoutTest() throws InterruptedException {
    NetworkReference network = getNetworkReference();
    targetReceived = new CompletableFuture<>();
    CompletableFuture<Void> sendCompleted = new CompletableFuture<>();

    network
        .component("A").input().port("in")
        .send("Word", message -> {
          assertTrue(message.failed());
          logger().info("Send failed: " + message.cause().getMessage());
          sendCompleted.complete(null);
          assertFalse(targetReceived.isDone());
        });

    CompletableFuture.allOf(sendCompleted, targetReceived).join();
    testComplete();

  }

  @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
  public static class ForwardingComponent extends MessageHandlerComponent<String> {

    @Override
    public void handle(VertigoMessage<String> event) {
      logger().info(name() + " forwarding");
      output()
          .port("out")
          .send(event.body(), event::handle);
    }
  }

  public static class TargetComponent extends MessageHandlerComponent<String> {

    @Override
    public void handle(VertigoMessage<String> event) {
      logger().info(name() + " received " + event.body());
      vertx.setTimer(100, id ->
      {
        logger().info(name() + " acking");
        targetReceived.complete(null);
        event.ack();
      });
    }

  }
}