package net.kuujo.vertigo.reference.impl;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.message.VertigoMessage;
import net.kuujo.vertigo.message.impl.VertigoMessageImpl;
import net.kuujo.vertigo.reference.NetworkOutputReference;

public class NetworkOutputReferenceImpl<T> implements NetworkOutputReference<T> {

  private final Vertx vertx;
  private final NetworkContext context;

  public NetworkOutputReferenceImpl(Vertx vertx, NetworkContext context) {
    this.vertx = vertx;
    this.context = context;
  }

  @Override
  public NetworkOutputReference<T> handler(Handler<VertigoMessage<T>> handler) {
    vertx.eventBus().<T>consumer(context.address()).handler(message -> {
      handler.handle(new VertigoMessageImpl<T>(message.headers().get("name"), message));
    });
    return this;
  }

}
