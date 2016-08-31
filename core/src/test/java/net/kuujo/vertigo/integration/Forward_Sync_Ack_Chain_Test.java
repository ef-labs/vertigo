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
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.builder.NetworkBuilder;
import net.kuujo.vertigo.reference.NetworkReference;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class Forward_Sync_Ack_Chain_Test extends VertigoTestBase {

  @Override
  protected NetworkConfig createNetwork() {
    NetworkBuilder builder = NetworkConfig.builder();
    builder
        .component("start").identifier(AutoForwardingComponent.class.getName())
        .component("first-1").identifier(AutoForwardingComponent.class.getName())
        .component("first-2").identifier(AutoForwardingComponent.class.getName())
        .component("second-1").identifier(AutoForwardingComponent.class.getName())
        .component("second-2").identifier(AutoForwardingComponent.class.getName())
        .component("final").identifier(AutoForwardingComponent.class.getName());

    builder.connect().network()
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

    builder
        .connect("final").port("done")
        .to().network();

    return builder.build();
  }

  @Test
  public void test() {
    AtomicInteger networkCounter = new AtomicInteger();
//    AtomicInteger componentCounter = new AtomicInteger();

    NetworkReference network = getNetworkReference();

    network.output()
        .handler(message -> {
          networkCounter.incrementAndGet();
          message.ack();
        });

    // TODO: Should this even be supported? May require additional config flag for port to be "external" = send to specific external address?
    // Output port is otherwise not included in the message, so the recipient (ref class) cannot listen to network output and filter there.
//    network.component("final")
//        .output()
//        .port("done")
//        .handler(event -> {
//          componentCounter.incrementAndGet();
//          event.ack();
//        });

    network
        .input()
        .send("Word", result -> {
          assertEquals(4, networkCounter.intValue());
//          assertEquals(4, componentCounter.intValue());
          testComplete();
        });

    await();
  }


}