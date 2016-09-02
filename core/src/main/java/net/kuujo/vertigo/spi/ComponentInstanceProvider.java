package net.kuujo.vertigo.spi;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.instance.ComponentInstance;

public interface ComponentInstanceProvider {

  void createInstance(Vertx vertx, JsonObject config, Future<ComponentInstance> componentFuture);

}
