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

import java.util.ArrayList;
import java.util.Collection;

import net.kuujo.vertigo.context.IOContext;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Input/output context.
 *
 * @author Jordan Halterman
 */
abstract class DefaultIOContext<T extends IOContext<T>> extends BaseContext<T> {
  protected Collection<DefaultOutputPortContext> streams = new ArrayList<>();
  @JsonIgnore
  private DefaultInstanceContext instance;

  /**
   * Sets the input parent.
   */
  @SuppressWarnings("unchecked")
  T setInstanceContext(DefaultInstanceContext instance) {
    this.instance = instance;
    return (T) this;
  }

  /**
   * Returns the parent instance context.
   *
   * @return The parent instance context.
   */
  public DefaultInstanceContext instance() {
    return instance;
  }

}
