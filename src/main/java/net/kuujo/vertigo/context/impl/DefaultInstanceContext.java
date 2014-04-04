/*
 * Copyright 2013-2014 the original author or authors.
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

import net.kuujo.vertigo.context.ComponentContext;
import net.kuujo.vertigo.context.InputContext;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.context.ModuleContext;
import net.kuujo.vertigo.context.OutputContext;
import net.kuujo.vertigo.context.VerticleContext;
import net.kuujo.vertigo.util.serializer.Serializer;
import net.kuujo.vertigo.util.serializer.SerializerFactory;

import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A component instance context which contains information regarding a specific component
 * (module or verticle) instance within a network.
 * 
 * @author Jordan Halterman
 */
public final class DefaultInstanceContext extends BaseContext<InstanceContext> implements InstanceContext {
  private int number;
  private String status;
  private DefaultInputContext input;
  private DefaultOutputContext output;
  @JsonIgnore
  private DefaultComponentContext<?> component;

  private DefaultInstanceContext() {
  }

  /**
   * Creates a new instance context from JSON.
   * 
   * @param context A JSON representation of the instance context.
   * @return A new instance context instance.
   * @throws MalformedContextException If the JSON context is malformed.
   */
  public static DefaultInstanceContext fromJson(JsonObject context) {
    Serializer serializer = SerializerFactory.getSerializer(DefaultInstanceContext.class);
    DefaultInstanceContext instance = serializer.deserializeObject(context.getObject("instance"), DefaultInstanceContext.class);
    DefaultComponentContext<?> component = DefaultComponentContext.fromJson(context);
    return instance.setComponentContext(component);
  }

  /**
   * Serializes an instance context to JSON.
   * 
   * @param context The instance context to serialize.
   * @return A Json representation of the instance context.
   */
  public static JsonObject toJson(InstanceContext context) {
    Serializer serializer = SerializerFactory.getSerializer(DefaultInstanceContext.class);
    JsonObject json = DefaultComponentContext.toJson(context.component().isModule() ?
        context.<ModuleContext>component() : context.<VerticleContext>component());
    return json.putObject("instance", serializer.serializeToObject(context));
  }

  /**
   * Sets the instance parent.
   */
  DefaultInstanceContext setComponentContext(DefaultComponentContext<?> component) {
    this.component = component;
    return this;
  }

  @Override
  public int number() {
    return number;
  }

  @Override
  public String address() {
    return address;
  }

  @Override
  public String status() {
    return status;
  }

  @Override
  public InputContext input() {
    return input.setInstanceContext(this);
  }

  @Override
  public OutputContext output() {
    return output.setInstanceContext(this);
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public <T extends ComponentContext> T component() {
    return (T) component;
  }

  @Override
  public void notify(InstanceContext update) {
    super.notify(update);
    input.notify(update.input());
    output.notify(update.output());
  }

  @Override
  public String toString() {
    return address();
  }

  /**
   * Instance context builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder extends BaseContext.Builder<Builder, DefaultInstanceContext> {

    private Builder() {
      super(new DefaultInstanceContext());
    }

    private Builder(DefaultInstanceContext context) {
      super(context);
    }

    /**
     * Creates a new context builder.
     *
     * @return A new instance context builder.
     */
    public static Builder newBuilder() {
      return new Builder();
    }

    /**
     * Creates a new context builder.
     *
     * @param context A starting instance context.
     * @return A new instance context builder.
     */
    public static Builder newBuilder(DefaultInstanceContext context) {
      return new Builder(context);
    }

    /**
     * Sets the instance status address.
     *
     * @param address The instance status address.
     * @return The context builder.
     */
    public Builder setStatusAddress(String address) {
      context.status = address;
      return this;
    }

    /**
     * Sets the instance number.
     *
     * @param number The instance number which should be unique to the component.
     * @return The context builder.
     */
    public Builder setNumber(int number) {
      context.number = number;
      return this;
    }

    /**
     * Sets the instance input context.
     *
     * @param input An input context.
     * @return The context builder.
     */
    public Builder setInput(DefaultInputContext input) {
      context.input = input;
      return this;
    }

    /**
     * Sets the instance output context.
     *
     * @param output An output context.
     * @return The context builder.
     */
    public Builder setOutput(DefaultOutputContext output) {
      context.output = output;
      return this;
    }
  }

}
