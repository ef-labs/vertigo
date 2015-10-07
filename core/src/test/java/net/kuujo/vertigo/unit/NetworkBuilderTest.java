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

import net.kuujo.vertigo.builder.NetworkBuilder;
import net.kuujo.vertigo.component.ComponentConfig;
import net.kuujo.vertigo.io.connection.ConnectionConfig;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.network.ValidationException;
import org.junit.Test;

import static org.junit.Assert.*;

public class NetworkBuilderTest {

  private static final String STUB_IDENTIFIER = StubComponent.class.getName();

  @Test
  public void networkBuilder_Create_Test() {

      // Build the network using "verbose" flow syntax
    NetworkBuilder builder = Network.builder()
            .name("network-1");

    builder.component()
            .name("sender")
            .identifier(STUB_IDENTIFIER)
            .output()
            .port("out");

    builder.component()
            .name("receiver")
            .identifier(STUB_IDENTIFIER)
            .input()
            .port("in");

    builder.connect()
            .component("sender")
            .port("out")
            .to("receiver")
            .port("in");

      // Verify the result
    Network network = builder.build();

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
    public void networkBuilder_ShortHand_Test1() {

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
      Network network = builder.build();

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
    public void networkBuilder_ShortHand_Test2() {

      // Build the network entirely through connection flow syntax
      NetworkBuilder builder = Network.builder()
          .name("network-1");

      builder.connect("sender")
          .identifier(STUB_IDENTIFIER)
          .port("out")
          .to("receiver")
          .identifier((STUB_IDENTIFIER))
          .port("in");

      // Verify the result
      Network network = builder.build();

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
    public void networkBuilder_Connections_And_Test() {

      // Build the network using the and() method.
      NetworkBuilder builder = Network.builder()
          .name("network-1");

      builder.connect()
          .component("sender").port("out").identifier(STUB_IDENTIFIER)
          .and("sender2").port("out2").identifier(STUB_IDENTIFIER)
          .to("receiver").port("in").identifier(STUB_IDENTIFIER)
          .and("receiver2").port("in2").identifier(STUB_IDENTIFIER);

      // Verify the result
      Network network = builder.build();

      assertEquals(4, network.getConnections().size());

      assertEquals(1, network.getConnections().stream().filter(
          n -> "sender".equals(n.getSource().getComponent())
              && "out".equals(n.getSource().getPort())
              && "receiver".equals(n.getTarget().getComponent())
              && "in".equals(n.getTarget().getPort())
      ).count());

      assertEquals(1, network.getConnections().stream().filter(
          n -> "sender2".equals(n.getSource().getComponent())
              && "out2".equals(n.getSource().getPort())
              && "receiver".equals(n.getTarget().getComponent())
              && "in".equals(n.getTarget().getPort())
      ).count());

      assertEquals(1, network.getConnections().stream().filter(
            n -> "sender".equals(n.getSource().getComponent())
              && "out".equals(n.getSource().getPort())
              && "receiver2".equals(n.getTarget().getComponent())
              && "in2".equals(n.getTarget().getPort())
      ).count());

        assertEquals(1, network.getConnections().stream().filter(
            n -> "sender2".equals(n.getSource().getComponent())
                && "out2".equals(n.getSource().getPort())
                && "receiver2".equals(n.getTarget().getComponent())
                && "in2".equals(n.getTarget().getPort())
        ).count());

    }

    @Test
    public void networkBuilder_Validate_Empty_Components_Test() {
      NetworkBuilder builder = Network.builder();

      try {
        builder.validate();
      }
      catch (ValidationException e) {
        assertEquals("Network components cannot be empty", e.getMessage());
      }

    }

    @Test
    public void networkBuilder_Validate_Empty_Connections_Test() {
      NetworkBuilder builder = Network.builder();
      builder.component("sender")
          .identifier(STUB_IDENTIFIER);

      try {
        builder.validate();
      }
      catch (ValidationException e) {
        assertEquals("Network connections cannot be empty", e.getMessage());
      }

    }

    @Test
    public void networkBuilder_Validate_Broken_Connection_Test() {
      NetworkBuilder builder = Network.builder();

      builder.connect("sender")
          .identifier(STUB_IDENTIFIER)
          .to("receiver")
          .identifier(STUB_IDENTIFIER);
      // Missing source and target ports

      try {
        builder.validate();
      }
      catch (ValidationException e) {
        assertTrue(e.getMessage().startsWith("Connection source port cannot be null"));
      }

    }

    @Test
    public void networkBuilder_Validate_Broken_Component_Test() {
      NetworkBuilder builder = Network.builder();

      // Components missing identifiers
      builder.connect("sender")
          .port("out")
          .to("receiver")
          .port("in");

      try {
          builder.validate();
      }
      catch (ValidationException e) {
          assertEquals("Component identifier cannot be null on sender", e.getMessage());
      }

    }

    @Test
    public void networkBuilder_Validate_Test() {
      NetworkBuilder builder = Network.builder();

      builder.connect("sender")
          .identifier(STUB_IDENTIFIER)
          .port("out")
          .to("receiver")
          .identifier(STUB_IDENTIFIER)
          .port("in");

      builder.validate();

    }

  @Test
  public void networkBuilder_Timeout_Test() {
    NetworkBuilder builder = Network.builder();

    builder.connect("sender")
        .identifier(STUB_IDENTIFIER)
        .port("out")
        .to("receiver")
        .identifier(STUB_IDENTIFIER)
        .port("in")
        .sendTimeout(1000);

    Network network = builder.build();

    ConnectionConfig connection = (ConnectionConfig)network.getConnections().toArray()[0];
    assertEquals(1000, connection.getSendTimeout());

  }

    public class StubComponent {
    }
    
}