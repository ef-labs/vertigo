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

import java.util.concurrent.atomic.AtomicInteger;

public class Network_Input_Output_Test extends VertigoTestBase {

  @Override
  protected NetworkConfig createNetwork() {
    NetworkBuilder builder = NetworkConfig.builder();
    builder
        .component("start").identifier(AutoForwardingComponent.class.getName());

    builder
        .connect().network()
        .to("start").port("in");

    builder
        .connect("start").port("done")
        .to().network();

    return builder.build();
  }

  @Test
  public void inputOutputTest() {
    NetworkReference network = getNetworkReference();
    AtomicInteger networkCounter = new AtomicInteger();

    network.output()
        .handler(message -> {
          logger().info("Received network output: " + message.body());
          networkCounter.incrementAndGet();
          message.ack();
        });

    network
        .input()
        .send("Word", result -> {
          logger().info("Send completed.");
          assertEquals(1, networkCounter.intValue());
          testComplete();
        });

    await();
  }

}