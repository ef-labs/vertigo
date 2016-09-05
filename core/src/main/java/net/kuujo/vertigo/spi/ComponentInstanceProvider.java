package net.kuujo.vertigo.spi;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.instance.ComponentInstance;

public interface ComponentInstanceProvider {
  void registerAndGet(Vertx vertx, JsonObject config, Handler<AsyncResult<ComponentInstance>> handler);
}
