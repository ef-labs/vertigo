package net.kuujo.vertigo.deployment.impl;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.context.ComponentContext;
import net.kuujo.vertigo.instance.ComponentInstance;
import net.kuujo.vertigo.spi.ComponentInstanceFactory;
import net.kuujo.vertigo.spi.ComponentInstanceProvider;

public class ConfigComponentContextManager implements ComponentInstanceProvider {

  ComponentInstanceFactory factory = ServiceHelper.loadFactory(ComponentInstanceFactory.class);

  @Override
  public void registerAndGet(Vertx vertx, JsonObject config, Handler<AsyncResult<ComponentInstance>> handler) {
    try {
      // Only supports configuration during component startup
      JsonObject contextJson = config.getJsonObject("vertigo_component_context");
      ComponentContext context = ComponentContext
          .builder()
          .update(contextJson)
          .build();
      ComponentInstance component = factory.createComponentInstance(vertx, context);
      component.start(result -> handler.handle(Future.succeededFuture(component)));

    } catch (Throwable throwable) {
      handler.handle(Future.failedFuture(throwable));
      throw throwable;
    }
  }
}
