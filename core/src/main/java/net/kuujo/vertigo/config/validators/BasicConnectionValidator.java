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

import net.kuujo.vertigo.config.ConnectionConfig;
import net.kuujo.vertigo.spi.ConnectionValidator;

/**
 * Basic connection validator.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class BasicConnectionValidator implements ConnectionValidator {

  @Override
  public void validate(ConnectionConfig connection) {
    if (connection.getSource() == null) {
      throw new ValidationException(String.format("Connection source cannot be null (%s)", connection.toJson().toString()));
    }
    if (connection.getSource().getComponent() == null) {
      throw new ValidationException(String.format("Connection source component cannot be null (%s)", connection.toJson().toString()));
    }
    if (connection.getSource().getPort() == null) {
      throw new ValidationException(String.format("Connection source port cannot be null (%s)", connection.toJson().toString()));
    }
    if (connection.getTarget() == null) {
      throw new ValidationException(String.format("Connection target cannot be null (%s)", connection.toJson().toString()));
    }
    if (connection.getTarget().getComponent() == null) {
      throw new ValidationException(String.format("Connection target component cannot be null (%s)", connection.toJson().toString()));
    }
    if (connection.getTarget().getPort() == null) {
      throw new ValidationException(String.format("Connection target port cannot be null (%s)", connection.toJson().toString()));
    }
  }

}
