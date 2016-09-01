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
package net.kuujo.vertigo.component;

import io.vertx.core.Verticle;
import net.kuujo.vertigo.context.ComponentContext;
import net.kuujo.vertigo.instance.ComponentInstance;
import net.kuujo.vertigo.instance.InputCollector;
import net.kuujo.vertigo.instance.OutputCollector;

import java.io.Serializable;

/**
 * Vertigo component.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface Component extends Verticle, Serializable {

  /**
   * Returns the component instance (which has the input and output objects.
   *
   * @return The component instance.
   */
  ComponentInstance component();

  /**
   * Returns the component's name in the network.
   */
  default String name() {
    return component().context().name();
  }

  /**
   * Returns the component context.
   *
   * The component context can be used to retrieve useful information about an
   * entire network.
   *
   * @return The component context.
   */
  default ComponentContext context() {
      return component().context();
    }

}
