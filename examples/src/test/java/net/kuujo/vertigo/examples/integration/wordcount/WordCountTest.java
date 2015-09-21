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


import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;
import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.builder.NetworkBuilder;
import net.kuujo.vertigo.component.AbstractComponent;
import net.kuujo.vertigo.examples.wordcount.WordCountNetwork;
import net.kuujo.vertigo.io.VertigoMessage;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * Word count network tests.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class WordCountTest extends VertxTestBase implements Handler<Message<Void>> {

  private static String address = UUID.randomUUID().toString();

  @Override
  public void setUp() throws Exception {
    super.setUp();

    NetworkBuilder builder = WordCountNetwork.networkBuilder();

    builder.component("done")
        .identifier(DoneComponent.class.getName())
        .input()
        .port("count");

    builder.connect()
        .component("word-counter")
        .port("count")
        .to("done")
        .port("count");

    vertx.eventBus().consumer(address, this);

    CountDownLatch latch = new CountDownLatch(1);

    vertx.runOnContext(aVoid -> {
      Vertigo vertigo = Vertigo.vertigo(vertx);
      vertigo.deployNetwork(builder.build(), result -> {
        if (result.failed()) {
          fail(result.cause().getMessage());
        }
        latch.countDown();
      });
    });

    latch.await();
  }

  @Test
  public void test() throws Exception {
    await();
  }

  @Override
  public void handle(Message<Void> message) {
    testComplete();
  }

  public static class DoneComponent extends AbstractComponent implements Handler<VertigoMessage<JsonObject>> {

    private boolean testComplete = false;

    @Override
    public void start() throws Exception {

      component()
          .input()
          .<JsonObject>port("count")
          .handler(this);

    }

    /**
     * Something has happened, so handle it.
     *
     * @param message the event to handle
     */
    @Override
    public void handle(VertigoMessage<JsonObject> message) {

      String word = message.body().getString("word");
      int count = message.body().getInteger("count", 0);
      message.ack();

      if (count >= 50 && !testComplete) {
        testComplete = true;
        vertx.eventBus().send(address, null);
      }

    }

  }

  //  public static class WordTester extends ComponentVerticle {
//    @Override
//    public void start() {
//      input.port("in").messageHandler(new Handler<String>() {
//        @Override
//        public void handle(String message) {
//          assertNotNull(message);
//          testComplete();
//        }
//      });
//    }
//  }

//  @Test
//  public void testRandomWordFeeder() {
//    final Vertigo vertigo = new Vertigo(this);
//    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<Cluster>>() {
//      @Override
//      public void handle(AsyncResult<Cluster> result) {
//        assertTrue(result.succeeded());
//        NetworkConfig network = vertigo.createNetwork("test");
//        network.addVerticle("feeder", WordCountNetwork.WordFeeder.class.getName());
//        network.addVerticle("tester", WordTester.class.getName());
//        network.createConnection("feeder", "word", "tester", "in");
//        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
//          @Override
//          public void handle(AsyncResult<ActiveNetwork> result) {
//            assertTrue(result.succeeded());
//          }
//        });
//      }
//    });
//  }

//  public static class CountTester extends ComponentVerticle {
//    private int count;
//    @Override
//    public void start() {
//      output.port("out").send("foo");
//      count++;
//      input.port("in").messageHandler(new Handler<JsonObject>() {
//        @Override
//        public void handle(JsonObject message) {
//          assertEquals("foo", message.getString("word"));
//          assertTrue(message.getInteger("count") == count);
//          if (count == 10) {
//            testComplete();
//          } else {
//            output.port("out").send("foo");
//            count++;
//          }
//        }
//      });
//    }
//  }

//  @Test
//  public void testWordCounter() {
//    final Vertigo vertigo = new Vertigo(this);
//    vertigo.deployCluster(UUID.randomUUID().toString(), new Handler<AsyncResult<Cluster>>() {
//      @Override
//      public void handle(AsyncResult<Cluster> result) {
//        assertTrue(result.succeeded());
//        NetworkConfig network = vertigo.createNetwork("test");
//        network.addVerticle("counter", WordCountNetwork.WordCounter.class.getName());
//        network.addVerticle("tester", CountTester.class.getName());
//        network.createConnection("counter", "count", "tester", "in");
//        network.createConnection("tester", "out", "counter", "word");
//        result.result().deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
//          @Override
//          public void handle(AsyncResult<ActiveNetwork> result) {
//            assertTrue(result.succeeded());
//          }
//        });
//      }
//    });
//  }

}