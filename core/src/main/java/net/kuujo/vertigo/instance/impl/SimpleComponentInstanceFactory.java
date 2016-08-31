package net.kuujo.vertigo.instance.impl;

import io.vertx.core.ServiceHelper;
import io.vertx.core.Vertx;
import net.kuujo.vertigo.context.*;
import net.kuujo.vertigo.instance.*;
import net.kuujo.vertigo.spi.ComponentInstanceFactory;
import net.kuujo.vertigo.spi.VertigoMessageFactory;

/**
 * Default ComponentInstanceFactory.
 */
public class SimpleComponentInstanceFactory implements ComponentInstanceFactory {

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
    return new SimpleInputConnection<>(vertx, connection, messageFactory);
  }

  @Override
  public <T> OutputConnection<T> createOutputConnection(Vertx vertx, OutputConnectionContext connection) {
    return new SimpleOutputConnection<>(vertx, connection);
  }

  @Override
  public <T> InputConnection<T> createExternalInputConnection(Vertx vertx, InputPortContext input) {
    return new ExternalInputConnection<>(vertx, input, messageFactory);
  }

}
