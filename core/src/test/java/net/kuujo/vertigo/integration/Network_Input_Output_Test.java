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

import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.builder.NetworkBuilder;
import net.kuujo.vertigo.reference.NetworkReference;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class Network_Input_Output_Test extends VertigoTestBase {

  static String address = UUID.randomUUID().toString();

  @Override
  protected NetworkConfig createNetwork() {
    NetworkBuilder builder = NetworkConfig.builder();
    builder
        .component("start").identifier(EventBusForwardingComponent.class.getName())
        .config(EventBusForwardingComponent.config(address));

    builder
        .connect()
        .network().port("in")
        .to("start").port("in");

    return builder.build();
  }

  @Test
  public void inputOutputTest() {
    NetworkReference network = getNetworkReference();

    vertx
        .eventBus()
        .consumer(address)
        .handler(event -> {
          logger().info("Received output: " + event.body());
          testComplete();
        });

    network
        .input()
        .port("in")
        .send("Word", result -> {
          logger().info("Send completed.");
        });

    await();
  }

}