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
package net.kuujo.vertigo.network;

import io.vertx.core.json.JsonObject;

/**
 * Base type for definitions.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface TypeConfig {

  /**
   * Applies a JSON configuration to the configuration.
   *
   * @param config The JSON configuration to apply.
   */
  void update(JsonObject config);

  /**
   * Returns a JSON representation of the configuration.
   *
   * @return A JSON object representation of the configuration.
   */
  JsonObject toJson();

}
