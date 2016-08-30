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
package net.kuujo.vertigo.config.validators;

import net.kuujo.vertigo.config.ComponentConfig;
import net.kuujo.vertigo.spi.ComponentValidator;

/**
 * Basic component validator implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class BasicComponentValidator implements ComponentValidator {

  @Override
  public void validate(ComponentConfig component) {
    if (component.getName() == null) {
      throw new ValidationException(String.format("Component name cannot be null %s", component.toJson().toString()));
    }
    if (component.getIdentifier() == null) {
      throw new ValidationException(String.format("Component identifier cannot be null on %s", component.getName()));
    }
  }

}
