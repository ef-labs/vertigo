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
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import net.kuujo.vertigo.builder.NetworkBuilder;
import net.kuujo.vertigo.io.VertigoMessage;
import net.kuujo.vertigo.network.Network;

import java.util.concurrent.CountDownLatch;

public class Execute_Ack_NoHandler_Test extends VertigoTestBase {

  static CountDownLatch targetReceivedLatch = new CountDownLatch(1);

  @Override
  protected Network createNetwork() {
    NetworkBuilder builder = Network.builder();
    builder
        .connect("A").identifier(StartComponent.class.getName()).port("out")
        .to("B").identifier(TargetComponent.class.getName()).port("in");
    return builder.build();
  }

  @Override
  protected void startEventComplete(AsyncResult<Message<Object>> result) throws Throwable {
    targetReceivedLatch.await();
    super.startEventComplete(result);
  }

  @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
  public static class StartComponent extends StartComponentBase {

    @Override
    protected void testStart(Handler<AsyncResult<Void>> completeHandler) {

      component()
          .output()
          .port("out")
          .send("Word");

      completeHandler.handle(Future.succeededFuture());

    }

  }

  public static class TargetComponent extends InputComponentBase<String> {

    @Override
    public void handle(VertigoMessage<String> event) {
      logger().info(component().context().name() + " received " + event.body());
      event.ack();
      targetReceivedLatch.countDown();
    }

  }

}