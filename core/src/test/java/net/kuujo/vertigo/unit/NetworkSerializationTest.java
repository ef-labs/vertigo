package net.kuujo.vertigo.unit;

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

import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.builder.NetworkBuilder;
import net.kuujo.vertigo.component.ComponentConfig;
import net.kuujo.vertigo.io.connection.ConnectionConfig;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.network.ValidationException;
import org.junit.Test;

import java.io.InputStream;
import java.util.Scanner;

import static org.junit.Assert.*;

public class NetworkSerializationTest {

  private static final String STUB_IDENTIFIER = StubComponent.class.getName();

    @Test
  public void networkBuilder_Serialize_Deserialize_Test() {

    // Build the network using "shorthand" flow syntax
    NetworkBuilder builder = Network.builder("network-1");

    builder.component("sender")
        .identifier(STUB_IDENTIFIER)
        .output()
        .port("out");

    builder.component("receiver")
        .identifier(STUB_IDENTIFIER)
        .input()
        .port("in");

    builder.connect("sender")
        .port("out")
        .to("receiver")
        .port("in");

    // Verify the result
    JsonObject json = builder.build().toJson();

    Network network = Network.network(json);

    ComponentConfig sender = network.getComponent("sender");
    assertNotNull(sender);
    assertEquals("sender", sender.getName());
    assertNotNull(sender.getOutput().getPort("out"));

    ComponentConfig receiver = network.getComponent("receiver");
    assertNotNull(receiver);
    assertEquals("receiver", receiver.getName());
    assertNotNull(receiver.getInput().getPort("in"));

    assertEquals(1, network.getConnections().size());
    ConnectionConfig connection = network.getConnections().stream().findFirst().get();
    assertNotNull(connection.getSource());
    assertEquals("sender", connection.getSource().getComponent());
    assertEquals("out", connection.getSource().getPort());
    assertNotNull(connection.getTarget());
    assertEquals("receiver", connection.getTarget().getComponent());
    assertEquals("in", connection.getTarget().getPort());
  }

  @Test
  public void networkBuilder_Deserialize_Simplified_Test() {

    // LoSad file
    InputStream networkStream = this.getClass().getClassLoader().getResourceAsStream("simple-network.json");
    String networkString = new Scanner(networkStream, "UTF-8").useDelimiter("\\A").next();
    JsonObject networkJson = new JsonObject(networkString);

    // Build the network
    Network network = Network.network(networkJson);

    // Verify the result
    ComponentConfig sender = network.getComponent("A");
    assertNotNull(sender);
    assertEquals("A", sender.getName());
    assertNotNull(sender.getOutput().getPort("out"));

    ComponentConfig receiver = network.getComponent("B");
    assertNotNull(receiver);
    assertEquals("B", receiver.getName());
    assertNotNull(receiver.getInput().getPort("in"));

    assertEquals(1, network.getConnections().size());
    ConnectionConfig connection = network.getConnections().stream().findFirst().get();
    assertNotNull(connection.getSource());
    assertEquals("A", connection.getSource().getComponent());
    assertEquals("out", connection.getSource().getPort());
    assertNotNull(connection.getTarget());
    assertEquals("B", connection.getTarget().getComponent());
    assertEquals("in", connection.getTarget().getPort());

  }

  public static class StubComponent {
  }

}