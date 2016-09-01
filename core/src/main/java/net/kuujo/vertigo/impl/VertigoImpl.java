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
package net.kuujo.vertigo.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import net.kuujo.vertigo.deployment.DeploymentManager;
import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.VertigoOptions;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.context.ContextBuilder;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.reference.NetworkReference;
import net.kuujo.vertigo.reference.impl.NetworkReferenceImpl;

/**
 * Vertigo implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class VertigoImpl implements Vertigo {
  private final Vertx vertx;
  private final VertigoOptions options;
  private final DeploymentManager manager;

  public VertigoImpl(Vertx vertx, VertigoOptions options) {
    this.vertx = vertx;
    this.options = options;
    this.manager = DeploymentManager.manager(vertx, options);
  }

  @Override
  public Vertigo network(String id, Handler<AsyncResult<NetworkReference>> resultHandler) {
    manager.getNetwork(id, result -> {
      if (result.failed()) {
        Future.<NetworkReference>failedFuture(result.cause()).setHandler(resultHandler);
      } else {
        Future.<NetworkReference>succeededFuture(new NetworkReferenceImpl(vertx, result.result())).setHandler(resultHandler);
      }
    });
    return this;
  }

  @Override
  public Vertigo deployNetwork(NetworkConfig network) {
    return deployNetwork(network, null);
  }

  @Override
  public Vertigo deployNetwork(NetworkConfig network, Handler<AsyncResult<NetworkReference>> doneHandler) {
    NetworkContext context = ContextBuilder.buildContext(network);
    manager.deployNetwork(context, result -> {
      if (result.failed()) {
        Future.<NetworkReference>failedFuture(result.cause()).setHandler(doneHandler);
      } else {
        Future.<NetworkReference>succeededFuture(new NetworkReferenceImpl(vertx, context)).setHandler(doneHandler);
      }
    });
    return this;
  }

  @Override
  public Vertigo undeployNetwork(String id) {
    return undeployNetwork(id, null);
  }

  @Override
  public Vertigo undeployNetwork(String id, Handler<AsyncResult<Void>> doneHandler) {
    manager.getNetwork(id, result -> {
      if (result.failed()) {
        Future.<Void>failedFuture(result.cause()).setHandler(doneHandler);
      } else {
        manager.undeployNetwork(result.result(), doneHandler);
      }
    });
    return this;
  }

}
