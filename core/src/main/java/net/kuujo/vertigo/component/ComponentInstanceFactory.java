package net.kuujo.vertigo.component;

import io.vertx.core.Vertx;
import net.kuujo.vertigo.io.InputCollector;
import net.kuujo.vertigo.io.InputContext;
import net.kuujo.vertigo.io.OutputCollector;
import net.kuujo.vertigo.io.OutputContext;
import net.kuujo.vertigo.io.connection.InputConnection;
import net.kuujo.vertigo.io.connection.InputConnectionContext;
import net.kuujo.vertigo.io.connection.OutputConnection;
import net.kuujo.vertigo.io.connection.OutputConnectionContext;
import net.kuujo.vertigo.io.port.InputPort;
import net.kuujo.vertigo.io.port.InputPortContext;
import net.kuujo.vertigo.io.port.OutputPort;
import net.kuujo.vertigo.io.port.OutputPortContext;

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
}
