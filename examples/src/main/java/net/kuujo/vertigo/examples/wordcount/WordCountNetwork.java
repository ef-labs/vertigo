package net.kuujo.vertigo.examples.wordcount;

import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.builder.NetworkBuilder;

/**
 * Builds a sample network for counting words.
 */
public class WordCountNetwork {

  public static NetworkConfig build(String resultAddress) {
    NetworkBuilder builder = NetworkConfig.builder();

    // Connect network input to word mapper
    builder
        .connect()
        .network()
        .port("input")
        .to("mapper")
        .identifier(WordMapper.class.getName())
        .port("input");

    // Connect mapper to counter
    builder
        .connect("mapper").port("words")
        .to("counter")
        .identifier(WordCounter.class.getName())
        .config(new JsonObject().put("result_address", resultAddress))
        .port("input");

    return builder.build();

  }

}
