package net.kuujo.vertigo.reference.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.reference.InputPortReference;
import net.kuujo.vertigo.util.AckAggregator;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Magnus.Koch on 9/1/2016.
 */
public class NetworkInputPortReference<T> implements InputPortReference<T> {

  private final Vertx vertx;
  private final String name;
  private final List<InputPortReferenceImpl<Object>> ports;

  private static final Logger logger = LoggerFactory.getLogger(NetworkInputPortReference.class);

  public NetworkInputPortReference(Vertx vertx, NetworkContext context, String name) {

    this.vertx = vertx;
    this.name = name;
    ports = context.config()
        .getConnections()
        .stream()
        .filter(connection ->
            connection.getSource().getIsNetwork()
                && name.equals(connection.getSource().getPort())
        )
        .map(connection -> {
          String address = context
              .component(connection.getTarget().getComponent())
              .address();
          return new InputPortReferenceImpl<>(vertx, address, connection.getTarget().getPort());
        })
        .collect(Collectors.toList());

    if (ports.size() == 0) {
      logger.info(
          "Dynamically created input port {} on network {}. The port has no connections.",
          name,
          context.name());
    }

  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public InputPortReference<T> send(T message) {
    ports.forEach(port -> port.send(message));
    return this;
  }

  @Override
  public InputPortReference<T> send(T message, MultiMap headers) {
    ports.forEach(port -> port.send(message, headers));
    return this;
  }

  @Override
  public InputPortReference<T> send(T message, Handler<AsyncResult<Void>> ackHandler) {
    AckAggregator acks = new AckAggregator();
    ports.forEach(port -> port.send(message, acks.increment()));
    acks.completed(ackHandler);
    return this;
  }

  @Override
  public InputPortReference<T> send(T message, MultiMap headers, Handler<AsyncResult<Void>> ackHandler) {
    AckAggregator acks = new AckAggregator();
    ports.forEach(port -> port.send(message, headers, acks.increment()));
    acks.completed(ackHandler);
    return this;
  }
}
