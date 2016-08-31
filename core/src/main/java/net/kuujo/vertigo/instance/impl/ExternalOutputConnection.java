package net.kuujo.vertigo.instance.impl;

import io.vertx.core.Vertx;
import net.kuujo.vertigo.context.OutputConnectionContext;

public class ExternalOutputConnection<T> extends SimpleOutputConnection<T> {

  public ExternalOutputConnection(Vertx vertx, OutputConnectionContext context) {
    super(vertx, context);
  }

}
