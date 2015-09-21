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
package net.kuujo.vertigo.examples.wordcount;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.builder.NetworkBuilder;
import net.kuujo.vertigo.component.AbstractComponent;
import net.kuujo.vertigo.io.ControllableOutput;
import net.kuujo.vertigo.io.VertigoMessage;
import net.kuujo.vertigo.io.port.OutputPort;
import net.kuujo.vertigo.network.Network;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * A word count network example.
 * <p>
 * This example demonstrates the use of selectors - in particular the HashSelector -
 * to control the dispersion of messages between multiple verticle instances.
 *
 * @author Jordan Halterman
 */
public class WordCountNetwork extends AbstractVerticle {

  /**
   * Random word feeder.
   */
  public static class WordFeeder extends AbstractComponent {
    private final String[] words = new String[]{
        "foo", "bar", "baz", "foobar", "foobaz", "barfoo", "barbaz", "bazfoo", "bazbar"
    };
    private final Random random = new Random();

    @Override
    public void start() {
      vertx.setTimer(500, id -> doSend());
    }

    private void doSend() {

      OutputPort<String> port = component().output().<String>port("word");

      if (port instanceof ControllableOutput) {
        ControllableOutput<?, ?> controllableOutput = (ControllableOutput<?, ?>)port;

        while (!controllableOutput.sendQueueFull()) {
          port.send(words[random.nextInt(words.length - 1)]);
        }
        controllableOutput.drainHandler(aVoid -> {
          doSend();
        });

      } else {

        // Just send 100 messages if not controllable
        for (int i = 0; i < 100; i++) {
          port.send(words[random.nextInt(words.length - 1)]);
        }

      }

    }
  }

  /**
   * Receives words on the "word" input port and maintains a
   * historical count of words received. Each time a word
   * is received its updated count is sent on the "count"
   * output port.
   */
  public static class WordCounter extends AbstractComponent implements Handler<VertigoMessage<String>> {
    private final Map<String, Integer> counts = new HashMap<>();

    /**
     * If your verticle does a simple, synchronous start-up then override this method and put your start-up
     * code in there.
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {

      component()
          .input()
          .<String>port("word")
          .handler(this);

    }

    /**
     * Something has happened, so handle it.
     *
     * @param message the event to handle
     */
    @Override
    public void handle(VertigoMessage<String> message) {

      String word = message.body();
      Integer count = counts.get(word);

      if (count == null) count = 0;
      counts.put(word, ++count);

      JsonObject msg = new JsonObject()
          .put("word", word)
          .put("count", count);

      component()
          .output()
          .<JsonObject>port("count")
          .send(msg, result -> {
            if (result.succeeded()) {

            }
          });

    }
  }

  @Override
  public void start(final Future<Void> startResult) {
    final Vertigo vertigo = Vertigo.vertigo(getVertx());

    vertigo.deployNetwork(networkBuilder().build(), result -> {
      if (result.succeeded()) {
        startResult.complete();
      } else {
        startResult.fail(result.cause());
      }
    });

  }

  public static NetworkBuilder networkBuilder() {

    NetworkBuilder builder = Network.builder().name("word-count");

    builder.component("word-feeder")
        .identifier(WordFeeder.class.getName())
        .worker(true)
        .output()
        .port("word");

    builder.component("word-counter")
        .identifier(WordCounter.class.getName())
        .input()
        .port("word")
        .output()
        .port("count");

    builder.connect()
        .component("word-feeder").port("word")
        .to("word-counter").port("word");

    return builder;

  }

}
