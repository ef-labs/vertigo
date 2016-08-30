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
import net.kuujo.vertigo.builder.NetworkBuilder;
import net.kuujo.vertigo.config.NetworkConfig;
import net.kuujo.vertigo.message.VertigoMessage;

public class Execute_Fail_Test extends VertigoTestBase {

  @Override
  protected NetworkConfig createNetwork() {
    NetworkBuilder builder = NetworkConfig.builder();
    builder
        .connect("A").identifier(StartComponent.class.getName()).port("out")
        .to("B").identifier(TargetComponent.class.getName()).port("in");
    return builder.build();
  }

  @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
  public static class StartComponent extends StartComponentBase {

    @Override
    protected void testStart(Handler<AsyncResult<Void>> completeHandler) {

      component()
          .output()
          .port("out")
          .send("Word", r -> {
            if (r.failed() && r.cause().getMessage().equals("Computer says no")) {
              logger().info(component().context().name() + " received failure: " + r.cause().getMessage());
              completeHandler.handle(Future.succeededFuture());
            } else {
              completeHandler.handle(Future.failedFuture(r.cause()));
            }
          });

    }

  }

  public static class TargetComponent extends InputComponentBase<String> {

    @Override
    public void handle(VertigoMessage<String> event) {
      logger().info(component().context().name() + " received " + event.body());
      event.fail(new RuntimeException("Computer says no"));
    }

  }

}