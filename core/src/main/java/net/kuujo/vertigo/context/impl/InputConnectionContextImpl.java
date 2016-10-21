/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.kuujo.vertigo.context.impl;

import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.context.InputConnectionContext;
import net.kuujo.vertigo.context.SourceContext;
import net.kuujo.vertigo.context.TargetContext;
import net.kuujo.vertigo.context.InputPortContext;
import net.kuujo.vertigo.instance.InputPort;
import net.kuujo.vertigo.util.Args;

/**
 * Input connection context implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class InputConnectionContextImpl implements InputConnectionContext {

  protected SourceContext source;
  protected TargetContext target;
  //  protected boolean ordered;
//  protected boolean atLeastOnce;
  protected long sendTimeout;
  protected InputPortContext port;

  @Override
  public SourceContext source() {
    return source;
  }

  @Override
  public TargetContext target() {
    return target;
  }

//  @Override
//  public boolean ordered() {
//    return ordered;
//  }
//
//  @Override
//  public boolean atLeastOnce() {
//    return atLeastOnce;
//  }

  @Override
  public long sendTimeout() {
    return sendTimeout;
  }

  @Override
  public InputPortContext port() {
    return port;
  }

  @Override
  public String toString(boolean formatted) {
    return toString();
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject()
        .put("source", source.toJson())
        .put("target", target.toJson())
        .put("sendTimeout", sendTimeout);
  }
  /**
   * Input connection context builder.
   */
  public static class Builder implements InputConnectionContext.Builder {
    private InputConnectionContextImpl connection;

    public Builder() {
      connection = new InputConnectionContextImpl();
    }

    public Builder(InputConnectionContextImpl connection) {
      this.connection = connection;
    }

    @Override
    public Builder setSource(SourceContext source) {
      Args.checkNotNull(source, "source cannot be null");
      connection.source = source;
      return this;
    }

    @Override
    public Builder setTarget(TargetContext target) {
      Args.checkNotNull(target, "target cannot be null");
      connection.target = target;
      return this;
    }

//    @Override
//    public Builder setOrdered(boolean ordered) {
//      connection.ordered = ordered;
//      return this;
//    }
//
//    @Override
//    public Builder setAtLeastOnce(boolean atLeastOnce) {
//      connection.atLeastOnce = atLeastOnce;
//      return this;
//    }

    @Override
    public Builder setPort(InputPortContext port) {
      Args.checkNotNull(port, "port cannot be null");
      connection.port = port;
      return this;
    }

    @Override
    public InputConnectionContext.Builder update(JsonObject json) {
      connection.source = SourceContext
          .builder()
          .update(json.getJsonObject("source"))
          .build();
      connection.target = TargetContext
          .builder()
          .update(json.getJsonObject("target"))
          .build();
      connection.sendTimeout = json.getLong("sendTimeout");
      return this;
    }

    @Override
    public InputConnectionContextImpl build() {
      return connection;
    }
  }

}
