package net.kuujo.vertigo.reference.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import net.kuujo.vertigo.network.TargetConfig;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.reference.InputPortReference;
import net.kuujo.vertigo.reference.NetworkInputReference;
import net.kuujo.vertigo.util.CountingCompletionHandler;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Magnus.Koch on 8/31/2016.
 */
public class NetworkInputReferenceImpl<T> implements NetworkInputReference<T> {

  private final Vertx vertx;
  private final NetworkContext context;
  private final List<InputPortReference<Object>> inputs;

  public NetworkInputReferenceImpl(Vertx vertx, NetworkContext context) {
    this.vertx = vertx;
    this.context = context;

    inputs = context.config()
        .getConnections()
        .stream()
        .filter(connectionConfig -> connectionConfig.getSource().getIsNetwork())
        .map(connectionConfig -> {
          TargetConfig target = connectionConfig.getTarget();
          ComponentReferenceImpl component = new ComponentReferenceImpl(vertx, context.component(target.getComponent()));
          return component.input().port(target.getPort());
        })
        .collect(Collectors.toList());

  }

  @Override
  public NetworkInputReference<T> send(T message) {
    inputs.forEach(portReference -> {
      portReference.send(message);
    });
    return this;
  }

  @Override
  public NetworkInputReference<T> send(T message, MultiMap headers) {
    inputs.forEach(portReference -> {
      portReference.send(message, headers);
    });
    return this;
  }

  @Override
  public NetworkInputReference<T> send(T message, Handler<AsyncResult<Void>> ackHandler) {
    CountingCompletionHandler<Void> completionHandler = new CountingCompletionHandler<>(inputs.size());
    completionHandler.setHandler(ackHandler);
    inputs.forEach(portReference -> {
      portReference.send(message, completionHandler);
    });
    return this;
  }

  @Override
  public NetworkInputReference<T> send(T message, MultiMap headers, Handler<AsyncResult<Void>> ackHandler) {
    CountingCompletionHandler<Void> completionHandler = new CountingCompletionHandler<>(inputs.size());
    completionHandler.setHandler(ackHandler);
    inputs.forEach(portReference -> {
      portReference.send(message, headers, completionHandler);
    });
    return this;
  }
}
