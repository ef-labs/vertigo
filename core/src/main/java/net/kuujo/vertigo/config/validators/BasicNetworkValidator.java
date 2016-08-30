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

import net.kuujo.vertigo.config.NetworkConfig;
import net.kuujo.vertigo.spi.NetworkValidator;

/**
 * Basic network validator.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class BasicNetworkValidator implements NetworkValidator {

  @Override
  public void validate(NetworkConfig network) {
    if (network.getName() == null) {
      throw new ValidationException("NetworkConfig name cannot be null");
    }
    if (network.getComponents() == null) {
      throw new ValidationException("NetworkConfig components cannot be null");
    }
    if (network.getComponents().isEmpty()) {
      throw new ValidationException("NetworkConfig components cannot be empty");
    }
    if (network.getConnections() == null) {
      throw new ValidationException("NetworkConfig connections cannot be null");
    }
    if (network.getConnections().isEmpty()) {
      throw new ValidationException("NetworkConfig connections cannot be empty");
    }
  }

}
