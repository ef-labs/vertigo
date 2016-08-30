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
import net.kuujo.vertigo.builder.NetworkBuilder;
import net.kuujo.vertigo.config.NetworkConfig;
import net.kuujo.vertigo.message.VertigoMessage;

import java.util.concurrent.atomic.AtomicInteger;

public class Execute_Ack_Chain_Test extends VertigoTestBase {

  static AtomicInteger endCounter = new AtomicInteger();

  @Override
  protected void startEventComplete(AsyncResult<Message<Object>> result) throws Throwable {
    assertEquals(4, endCounter.intValue());
    super.startEventComplete(result);
  }

  @Override
  protected NetworkConfig createNetwork() {
    NetworkBuilder builder = NetworkConfig.builder();
    builder
        .component("start").identifier(StartComponent.class.getName())
        .component("middle-1").identifier(MiddleComponent.class.getName())
        .component("middle-2").identifier(MiddleComponent.class.getName())
        .component("end-1").identifier(EndComponent.class.getName())
        .component("end-2").identifier(EndComponent.class.getName());

    builder.connect("start").port("out")
        .to("middle-1").port("in")
        .and("middle-2").port("in")
    ;

    builder
        .connect("middle-1").port("out")
        .to("end-1").port("in")
        .and("end-2").port("in");

    builder
        .connect("middle-2").port("out")
        .to("end-1").port("in2")
        .and("end-2").port("in2");

    return builder.build();
  }

  @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
  public static class StartComponent extends StartComponentBase {

    @Override
    protected void testStart(Handler<AsyncResult<Void>> completeHandler) {
      component()
          .output()
          .port("out")
          .send("Word", completeHandler);
    }

  }

  public static class MiddleComponent extends InputComponentBase<String> {
    @Override
    public void handle(VertigoMessage<String> message) {
      logger().info(component().context().name() + " received message " + message.body());

      // Transform message
      String reverse = new StringBuffer(message.body()).reverse().toString();

      component()
          .output()
          .port("out")
          .send(reverse, message::handle);

    }
  }

  public static class EndComponent extends InputComponentBase<String> {
    @Override
    public void handle(VertigoMessage<String> message) {
      endCounter.incrementAndGet();
      logger().info(component().context().name() + " received message " + message.body());
      message.ack();
    }
  }

}