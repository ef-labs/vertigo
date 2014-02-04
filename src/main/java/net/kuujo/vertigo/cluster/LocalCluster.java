/*
 * Copyright 2013 the original author or authors.
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
package net.kuujo.vertigo.cluster;

import java.util.Set;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;

/**
 * A local cluster implementation.<p>
 *
 * The local cluster deploys networks to the local Vert.x instance using the
 * Vert.x container.
 *
 * @author Jordan Halterman
 */
public class LocalCluster extends AbstractCluster {
  private final Container container;

  public LocalCluster(Verticle verticle) {
    super(verticle.getVertx().eventBus(), LoggerFactory.getLogger(LocalCluster.class));
    this.container = verticle.getContainer();
  }

  public LocalCluster(Vertx vertx, Container container) {
    super(vertx.eventBus(), LoggerFactory.getLogger(LocalCluster.class));
    this.container = container;
  }

  @Override
  protected void deployVerticle(String main, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler) {
    container.deployVerticle(main, config, instances, doneHandler);
  }

  @Override
  protected void deployVerticleTo(Set<String> nodes, String main, JsonObject config, int instances,
      Handler<AsyncResult<String>> doneHandler) {
    deployVerticle(main, config, instances, doneHandler);
  }

  @Override
  protected void deployWorkerVerticle(String main, JsonObject config, int instances, boolean multiThreaded,
      Handler<AsyncResult<String>> doneHandler) {
    container.deployWorkerVerticle(main, config, instances, multiThreaded, doneHandler);
  }

  @Override
  protected void deployWorkerVerticleTo(Set<String> nodes, String main, JsonObject config, int instances,
      boolean multiThreaded, Handler<AsyncResult<String>> doneHandler) {
    deployWorkerVerticle(main, config, instances, multiThreaded, doneHandler);
  }

  @Override
  protected void deployModule(String main, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler) {
    container.deployModule(main, config, instances, doneHandler);
  }

  @Override
  protected void deployModuleTo(Set<String> nodes, String main, JsonObject config, int instances,
      Handler<AsyncResult<String>> doneHandler) {
    deployModule(main, config, instances, doneHandler);
  }

}
