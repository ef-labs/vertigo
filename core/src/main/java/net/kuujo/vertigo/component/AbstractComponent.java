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
import io.vertx.core.ServiceHelper;
import io.vertx.core.VertxException;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.spi.ComponentInstanceProvider;
import net.kuujo.vertigo.instance.ComponentInstance;

/**
 * Abstract Java component.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public abstract class AbstractComponent extends AbstractVerticle implements Component {

  static ComponentInstanceProvider instanceProvider = ServiceHelper.loadFactory(ComponentInstanceProvider.class);

  private ComponentInstance component;

  /**
   * Start the verticle.<p>
   * This is called by Vert.x when the verticle instance is deployed. Don't call it yourself.<p>
   * It is recommended that you override initComponent instead, where the component is already initialized.
   *
   * @param startFuture a future which should be called when verticle start-up is completed.
   * @throws Exception
   */
  @Override
  public void start(Future<Void> startFuture) throws Exception {

    Future<ComponentInstance> componentFuture = Future.<ComponentInstance>future()
        .setHandler(componentInstanceAsyncResult -> {
          if (componentInstanceAsyncResult.succeeded()) {
            this.component = componentInstanceAsyncResult.result();

            Future<Void> initFuture = Future.<Void>future()
                .setHandler(initResult -> {
                  if (initResult.succeeded()) {
                    try {
                      super.start(startFuture);
                    } catch (Exception e) {
                      // Start threw an exception
                      this.component.stop();
                      startFuture.fail(e);
                    }
                  } else {
                    // InitComponent failed
                    this.component.stop();
                    startFuture.fail(initResult.cause());
                  }
                });

            try {
              initComponent(initFuture);
            } catch (Exception e) {
              this.component.stop();
              startFuture.fail(e);
            }

          } else {
            // Component creation failed
            startFuture.fail(componentInstanceAsyncResult.cause());
          }
        });

    // Create the component instance
    instanceProvider.createInstance(vertx, config(), componentFuture);

  }

  /**
   * Override this method to register input port handlers and other initialization work.
   */
  protected void initComponent(Future<Void> initFuture) throws Exception {
    initComponent();
    initFuture.complete();
  };

  /**
   * Override this method to register input port handlers and other initialization work.
   */
  protected void initComponent() throws Exception {
  };

  /**
   * Stop the verticle.<p>
   * This is called by Vert.x when the verticle instance is un-deployed. Don't call it yourself.<p>
   * If your verticle does things in it's shut-down which take some time then you can override this method
   * and call the stopFuture some time later when clean-up is completed.
   *
   * @param stopFuture a future which should be called when verticle clean-up is completed.
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

  @Override
  public ComponentInstance component() {
    return component;
  }

}
