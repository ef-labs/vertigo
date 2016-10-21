package net.kuujo.vertigo.reference.impl;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.reference.InputPortReference;
import net.kuujo.vertigo.reference.InputReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Magnus.Koch on 8/31/2016.
 */
public class NetworkInputReferenceImpl implements InputReference {

    private final static Logger logger = LoggerFactory.getLogger(NetworkInputReferenceImpl.class);

    private final Vertx vertx;
    private final NetworkContext context;
    private final Map<String, InputPortReference> ports;

  public NetworkInputReferenceImpl(Vertx vertx, NetworkContext context) {
    this.vertx = vertx;
    this.context = context;
    ports = context.config()
        .getConnections()
        .stream()
        .filter(connectionConfig -> connectionConfig.getSource().getIsNetwork())
        .map(connectionConfig -> connectionConfig.getSource().getPort() )
        .distinct()
        .collect(Collectors.toConcurrentMap(
            port -> port,
            port -> new NetworkInputPortReference(vertx, context, port)));
  }

  @Override
  public <T> InputPortReference<T> port(String name) {
    InputPortReference port = ports.get(name);
    if (port == null) {
      port = new NetworkInputPortReference(vertx, context, "name");
      if (logger.isInfoEnabled()) {
        logger.info(
            String.format(
                "Dynamically created output port %s on network %s. The port has no connections.",
                name,
                context.name()));
      }
    }
    return port;
  }

  @Override
  public List<InputPortReference> ports() {
    return new ArrayList<>(ports.values());
  }

  //  private final Map<String, List<String>> addresses = new HashMap<>();
  //
  //  public NetworkInputReferenceImpl(Vertx vertx, NetworkContext context) {
  //    this.vertx = vertx;
  //    this.context = context;
  //
  //    // Group connections by network source port
  //    Map<String, List<ConnectionConfig>> connections = context.config()
  //        .getConnections()
  //        .stream()
  //        .filter(connection -> connection.getSource().getIsNetwork())
  //        .collect(Collectors.groupingBy(connection -> connection.getSource().getPort()));
  //
  //    // Convert to [port] > [list of addresses]
  //    for (Map.Entry<String, List<ConnectionConfig>> entry : connections.entrySet()) {
  //
  //      List<String> addresses = entry
  //          .getValue()
  //          .stream()
  //          .map(connection -> {
  //            ComponentContext targetContext = context.component(connection.getTarget().getComponent());
  //            return targetContext.address();
  //          })
  //          .collect(Collectors.toList());
  //
  //      this.addresses.put(entry.getKey(), addresses);
  //    }
  //
  //  }
  //
  //  @Override
  //  public <T> InputPortReference<T> port(String name) {
  //    InputPortReferenceImpl<T> targetPort = new InputPortReferenceImpl<>(vertx, address, name);
  //    return (InputPortReference<T>) port;
  //  }
  //
//  @Override
//  public NetworkInputReference<T> send(T message) {
//    inputs.forEach(portReference -> {
//      portReference.send(message);
//    });
//    return this;
//  }
//
//  @Override
//  public NetworkInputReference<T> send(T message, MultiMap headers) {
//    inputs.forEach(portReference -> {
//      portReference.send(message, headers);
//    });
//    return this;
//  }
//
//  @Override
//  public NetworkInputReference<T> send(T message, Handler<AsyncResult<Void>> ackHandler) {
//    CountingCompletionHandler<Void> completionHandler = new CountingCompletionHandler<>(inputs.size());
//    completionHandler.setHandler(ackHandler);
//    inputs.forEach(portReference -> {
//      portReference.send(message, completionHandler);
//    });
//    return this;
//  }
//
//  @Override
//  public NetworkInputReference<T> send(T message, MultiMap headers, Handler<AsyncResult<Void>> ackHandler) {
//    CountingCompletionHandler<Void> completionHandler = new CountingCompletionHandler<>(inputs.size());
//    completionHandler.setHandler(ackHandler);
//    inputs.forEach(portReference -> {
//      portReference.send(message, headers, completionHandler);
//    });
//    return this;
//  }


}
