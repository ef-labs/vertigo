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
package net.kuujo.vertigo.deployment.impl;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.spi.VerticleFactory;
import net.kuujo.vertigo.deployment.DeploymentManager;
import net.kuujo.vertigo.VertigoException;
import net.kuujo.vertigo.context.ComponentContext;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.util.CountingCompletionHandler;

/**
 * Local context manager implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class LocalDeploymentManager implements DeploymentManager {
  private static final String NETWORKS_KEY = "vertigo";
  private final Vertx vertx;

  LocalDeploymentManager(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public DeploymentManager getNetwork(String id, Handler<AsyncResult<NetworkContext>> doneHandler) {
    NetworkContext context = vertx.sharedData().<String, NetworkContext>getLocalMap(NETWORKS_KEY).get(id);
    if (context == null) {
      Future.<NetworkContext>failedFuture(new VertigoException(String.format("Invalid network %s: NetworkConfig not found", id))).setHandler(doneHandler);
    } else {
      Future.succeededFuture(context).setHandler(doneHandler);
    }
    return this;
  }

  @Override
  public DeploymentManager deployNetwork(NetworkContext network, Handler<AsyncResult<Void>> doneHandler) {

    // Add to local map to make it accessible from the component.start() methods
    vertx.sharedData().<String, NetworkContext>getLocalMap(NETWORKS_KEY).put(network.name(), network);

    CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(network.components().size()).setHandler(result -> {
      if (result.failed()) {
        vertx.sharedData().<String, NetworkContext>getLocalMap(NETWORKS_KEY).remove(network.name());
      }
      doneHandler.handle(result);
    });

    for (ComponentContext component : network.components()) {

      JsonObject config = new JsonObject()
          .put("vertigo_component_id", component.name())
          .put("vertigo_network_id", network.name());
      if (component.config() != null) {
        config.mergeIn(component.config());
      }

      DeploymentOptions options = new DeploymentOptions()
          .setConfig(config)
          .setWorker(component.worker())
          .setMultiThreaded(component.multiThreaded());

      vertx.deployVerticle(component.main(), options, result -> {
        if (result.failed()) {
          counter.fail(result.cause());
        } else {
          vertx.sharedData().<String, String>getLocalMap(network.name()).put(component.address(), result.result());
          counter.succeed();
        }
      });
    }
    return this;
  }

  @Override
  public DeploymentManager undeployNetwork(NetworkContext network, Handler<AsyncResult<Void>> doneHandler) {
    LocalMap<String, String> deploymentIds = vertx.sharedData().getLocalMap(network.name());

    CountingCompletionHandler<Void> counter = new CountingCompletionHandler<Void>(deploymentIds.size()).setHandler(result -> {
      if (result.succeeded()) {
        vertx.sharedData().<String, NetworkContext>getLocalMap(NETWORKS_KEY).remove(network.name());
        vertx.sharedData().<String, String>getLocalMap(network.name()).clear();
      }
      doneHandler.handle(result);
    });

    for (ComponentContext component : network.components()) {
      String deploymentId = deploymentIds.get(component.address());
      if (deploymentId != null) {
        vertx.undeploy(deploymentId, counter);
      }
    }
    return this;
  }

}
