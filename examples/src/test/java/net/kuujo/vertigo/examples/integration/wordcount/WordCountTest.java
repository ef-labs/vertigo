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
package net.kuujo.vertigo.examples.integration.wordcount;


import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.test.core.VertxTestBase;
import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.examples.wordcount.WordCountNetwork;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.reference.NetworkReference;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Word count network tests.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class WordCountTest extends VertxTestBase {

  private static Logger logger = LoggerFactory.getLogger(WordCountTest.class);

  private String resultAddress = UUID.randomUUID().toString();
  private NetworkReference network;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    NetworkConfig networkConfig = WordCountNetwork.build(resultAddress);

    // Deploy network
    CompletableFuture<Void> future = new CompletableFuture<>();
    vertx.runOnContext(aVoid -> {
      Vertigo vertigo = Vertigo.vertigo(vertx);
      vertigo.deployNetwork(networkConfig, result -> {
        if (result.failed()) {
          fail(result.cause().getMessage());
        }
        this.network = result.result();
        future.complete(null);
      });
    });

    future.join();
  }

  @Test
  public void loremIpsumTest() throws Exception {
    AtomicInteger highestCount = new AtomicInteger();

    // Add output handler to track highest count
    vertx.eventBus()
        .consumer(resultAddress, event -> {
          Integer count = (Integer)event.body();
          highestCount.accumulateAndGet(count, Math::max);
        });

    network
        .input()
        .port("input")
        .send("Lorem ipsum dolor sit amet, consectetur adipiscing elit", event -> {
          assertTrue(event.succeeded());
          logger.info("Send complete.");

          // Wait a bit to allow event bus to drain
          vertx.setTimer(50, id -> {
            assertEquals(8, event.result());
            assertEquals(8, highestCount.get());
            logger.info("Test complete: " + highestCount.get());
            testComplete();
          });

        });

    await();
  }

  @Test
  public void emptyStringTest() throws Exception {

    // Add output handler to track highest count
    vertx.eventBus()
        .consumer(resultAddress, event -> {
          fail("No word count expected from empty string.");
        });

    network
        .input()
        .port("input")
        .send("", event -> {
          assertTrue(event.succeeded());
          vertx.setTimer(50, id -> testComplete());
        });

    await();
  }

}