package net.kuujo.vertigo.spi;

import io.vertx.core.Vertx;
import net.kuujo.vertigo.context.ComponentContext;
import net.kuujo.vertigo.instance.*;
import net.kuujo.vertigo.context.InputContext;
import net.kuujo.vertigo.context.OutputContext;
import net.kuujo.vertigo.context.InputConnectionContext;
import net.kuujo.vertigo.context.OutputConnectionContext;
import net.kuujo.vertigo.context.InputPortContext;
import net.kuujo.vertigo.context.OutputPortContext;

/**
 * Factory for constructing network component instances.
 */
public interface ComponentInstanceFactory {
    ComponentInstance createComponentInstance(Vertx vertx, ComponentContext context);
    InputCollector createInputCollector(Vertx vertx, InputContext input);
    OutputCollector createOutputCollector(Vertx vertx, OutputContext output);
    InputPort createInputPort(Vertx vertx, InputPortContext input);
    OutputPort createOutputPort(Vertx vertx, OutputPortContext output);
    <T> InputConnection<T> createInputConnection(Vertx vertx, InputConnectionContext connection);
    <T> OutputConnection<T> createOutputConnection(Vertx vertx, OutputConnectionContext connection);
    <T> InputConnection<T> createExternalInputConnection(Vertx vertx, InputPortContext input);
}
