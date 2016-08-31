package net.kuujo.vertigo.examples.wordcount;

import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.builder.NetworkBuilder;

/**
 * Builds a sample network for counting words.
 */
public class WordCountNetwork {

  public static NetworkConfig build() {
    NetworkBuilder builder = NetworkConfig.builder();

    // Connect network input to word mapper
    builder
        .connect().network()
        .to("mapper")
        .identifier(WordMapper.class.getName())
        .port("input");

    // Connect mapper to counter
    builder
        .connect("mapper").port("words")
        .to("counter")
        .identifier(WordCounter.class.getName())
        .port("input");

    // Connect counter to network output
    builder
        .connect("counter")
        .port("count")
        .to().network();

    return builder.build();
  }

}
