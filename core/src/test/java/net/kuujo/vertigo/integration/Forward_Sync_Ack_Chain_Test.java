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

import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.network.builder.NetworkBuilder;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.builder.NetworkBuilder;
import net.kuujo.vertigo.reference.NetworkReference;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class Forward_Sync_Ack_Chain_Test extends VertigoTestBase {

  public static String address = UUID.randomUUID().toString();

  @Override
  protected NetworkConfig createNetwork() {

    NetworkBuilder builder = NetworkConfig.builder();
    builder
        .component("start").identifier(AutoForwardingComponent.class.getName())
        .component("first-1").identifier(AutoForwardingComponent.class.getName())
        .component("first-2").identifier(AutoForwardingComponent.class.getName())
        .component("second-1").identifier(AutoForwardingComponent.class.getName())
        .component("second-2").identifier(AutoForwardingComponent.class.getName())
        .component("final").identifier(EventBusForwardingComponent.class.getName())
        .config(EventBusForwardingComponent.config(address));

    builder.connect()
        .network().port("in")
        .to("start").port("in");

    builder.connect("start").port("out")
        .to("first-1").port("in")
        .and("first-2").port("in");

    builder
        .connect("first-1").port("out")
        .to("second-1").port("in")
        .and("second-2").port("in");

    builder
        .connect("first-2").port("out")
        .to("second-1").port("in")
        .and("second-2").port("in");

    builder
        .connect("second-1").port("out")
        .and("second-2").port("out")
        .to("final").port("in");

    return builder.build();
  }

  @Test
  public void test() {
    AtomicInteger networkCounter = new AtomicInteger();
    CompletableFuture<Void> receivedEnough = new CompletableFuture<>();

    vertx
        .eventBus()
        .consumer(address)
        .handler(event -> {
          int count = networkCounter.incrementAndGet();
          logger().info("Received " + event.body() + ", count = " + count);
          if (count >= 4) {
            receivedEnough.complete(VOID);
          }
        });

    NetworkReference network = getNetworkReference();
    network
        .input()
        .port("in")
        .send("Word", result -> {
          receivedEnough.whenComplete((aVoid, throwable) -> {
            // Wait some to make sure there are no stray event bus messages
            vertx.setTimer(50, id -> {
              assertEquals(4, networkCounter.intValue());
              testComplete();
            });
          });
        });

    await();
  }


}