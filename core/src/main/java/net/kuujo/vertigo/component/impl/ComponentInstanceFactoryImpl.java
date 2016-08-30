package net.kuujo.vertigo.component.impl;

import io.vertx.core.ServiceHelper;
import io.vertx.core.Vertx;
import net.kuujo.vertigo.component.ComponentContext;
import net.kuujo.vertigo.component.ComponentInstance;
import net.kuujo.vertigo.component.ComponentInstanceFactory;
import net.kuujo.vertigo.io.*;
import net.kuujo.vertigo.io.connection.InputConnection;
import net.kuujo.vertigo.io.connection.InputConnectionContext;
import net.kuujo.vertigo.io.connection.OutputConnection;
import net.kuujo.vertigo.io.connection.OutputConnectionContext;
import net.kuujo.vertigo.io.connection.impl.InputConnectionImpl;
import net.kuujo.vertigo.io.connection.impl.OutputConnectionImpl;
import net.kuujo.vertigo.io.impl.InputCollectorImpl;
import net.kuujo.vertigo.io.impl.OutputCollectorImpl;
import net.kuujo.vertigo.io.port.InputPort;
import net.kuujo.vertigo.io.port.InputPortContext;
import net.kuujo.vertigo.io.port.OutputPort;
import net.kuujo.vertigo.io.port.OutputPortContext;
import net.kuujo.vertigo.io.port.impl.InputPortImpl;
import net.kuujo.vertigo.io.port.impl.OutputPortImpl;

/**
 * Default ComponentInstanceFactory.
 */
public class ComponentInstanceFactoryImpl implements ComponentInstanceFactory {

  final VertigoMessageFactory messageFactory = ServiceHelper.loadFactory(VertigoMessageFactory.class);

  @Override
  public ComponentInstance createComponentInstance(Vertx vertx, ComponentContext context) {
    return new ComponentInstanceImpl(vertx, context, this);
  }

  @Override
  public InputCollector createInputCollector(Vertx vertx, InputContext input) {
    return new InputCollectorImpl(vertx, input, this);
  }

  @Override
  public OutputCollector createOutputCollector(Vertx vertx, OutputContext output) {
    return new OutputCollectorImpl(vertx, output, this);
  }

  @Override
  public InputPort createInputPort(Vertx vertx, InputPortContext inputPort) {
    return new InputPortImpl<>(vertx, inputPort, this);
  }

  @Override
  public OutputPort createOutputPort(Vertx vertx, OutputPortContext outputPort) {
    return new OutputPortImpl<>(vertx, outputPort, this);
  }

  @Override
  public <T> InputConnection<T> createInputConnection(Vertx vertx, InputConnectionContext connection) {
    return new InputConnectionImpl<T>(vertx, connection, messageFactory);
  }

  @Override
  public <T> OutputConnection<T> createOutputConnection(Vertx vertx, OutputConnectionContext connection) {
    return new OutputConnectionImpl<T>(vertx, connection);
  }

}
