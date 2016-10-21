package net.kuujo.vertigo.examples.wordcount;

import net.kuujo.vertigo.component.MessageHandlerComponent;
import net.kuujo.vertigo.message.VertigoMessage;
import net.kuujo.vertigo.util.AckAggregator;

/**
 * Vertigo component which splits incoming strings into words.
 * The words are then forwarded as individual messages.
 */
public class WordMapper extends MessageHandlerComponent<String> {

  @Override
  public void handle(VertigoMessage<String> message) {

    // Split to words
    String input = message.body();
    String[] words = input.split(" ");

    // Forward words individually, and ack when they are all completed
    AckAggregator acks = new AckAggregator();
    for (String word : words) {
      output().port("words").send(word, acks.increment());
    }
    acks.completed(message::handle);

  }

}
