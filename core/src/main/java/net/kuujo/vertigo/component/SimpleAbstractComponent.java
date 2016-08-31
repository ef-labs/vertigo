/*
 * Copyright 2014 the original author or authors.
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
package net.kuujo.vertigo.component;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.ServiceHelper;
import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.VertigoException;
import net.kuujo.vertigo.VertigoOptions;
import net.kuujo.vertigo.context.ComponentContext;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.deployment.DeploymentManager;
import net.kuujo.vertigo.instance.ComponentInstance;
import net.kuujo.vertigo.instance.OutputCollector;
import net.kuujo.vertigo.message.VertigoMessage;
import net.kuujo.vertigo.spi.ComponentInstanceFactory;

/**
 * Abstract Java component.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public abstract class SimpleAbstractComponent<T> extends AbstractComponent implements Handler<VertigoMessage<T>> {

  public OutputCollector output() {
    return component().output();
  }

  @Override
  public void start() throws Exception {

    // Register all input ports automatically
    component()
        .input()
        .ports()
        .forEach(port -> {
          component()
              .input()
              .<T>port(port.name())
              .handler(this);
        });

  }

}
