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
import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.ContextManager;
import net.kuujo.vertigo.VertigoException;
import net.kuujo.vertigo.VertigoOptions;
import net.kuujo.vertigo.component.impl.ComponentInstanceImpl;
import net.kuujo.vertigo.network.NetworkContext;

/**
 * Abstract Java component.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public abstract class AbstractComponent extends AbstractVerticle implements Component {

  private ComponentInstance component;
  private NetworkContext network;

  @Override
  public ComponentInstance component() {
    return component;
  }

  /**
   * Start the verticle.<p>
   * This is called by Vert.x when the verticle instance is deployed. Don't call it yourself.<p>
   * If your verticle does things in it's startup which take some time then you can override this method
   * and call the startFuture some time later when start up is complete.
   *
   * @param startFuture a future which should be called when verticle start-up is complete.
   * @throws Exception
   */
  @Override
  public void start(Future<Void> startFuture) throws Exception {

    ContextManager manager = ContextManager.manager(vertx, new VertigoOptions());

    // TODO: How are you supposed to create ComponentInstance?  From NetworkContext?
    JsonObject config = config();
    String id = config.getString("vertigo_component_id");
    String networkId = config.getString("vertigo_network_id");

    manager.getNetwork(networkId, result -> {
      if (result.succeeded()) {
        network = result.result();

        ComponentContext cc = network.component(id);
        if (cc == null) {
          startFuture.fail(new VertigoException("ComponentContext " + id + " does not exist in the network"));
          return;
        }

        component = new ComponentInstanceImpl(vertx, cc);
        component.start(result2 -> {
          if (result2.succeeded()) {
            try {
              AbstractComponent.super.start(startFuture);
            } catch (Exception e) {
              startFuture.fail(e);
            }
          } else {
            startFuture.fail(result2.cause());
          }
        });
      } else {
        startFuture.fail(result.cause());
      }
    });

  }

  /**
   * Stop the verticle.<p>
   * This is called by Vert.x when the verticle instance is un-deployed. Don't call it yourself.<p>
   * If your verticle does things in it's shut-down which take some time then you can override this method
   * and call the stopFuture some time later when clean-up is complete.
   *
   * @param stopFuture a future which should be called when verticle clean-up is complete.
   * @throws Exception
   */
  @Override
  public void stop(Future<Void> stopFuture) throws Exception {

    if (component != null) {
      component.stop(result -> {
        if (result.succeeded()) {
          try {
            AbstractComponent.super.stop(stopFuture);
          } catch (Exception e) {
            stopFuture.fail(e);
          }
        } else {
          stopFuture.fail(result.cause());
        }
      });
      component = null;
    }

  }
}
