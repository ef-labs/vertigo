package net.kuujo.vertigo.examples.wordcount;

import net.kuujo.vertigo.component.MessageHandlerComponent;
import net.kuujo.vertigo.message.VertigoMessage;

/**
 * Vertigo component which counts incoming words and sends out the latest word count.
 */
public class WordCounter extends MessageHandlerComponent<String> {

  private Integer count = 0;

  @Override
  public void handle(VertigoMessage<String> message) {
    if (message.body().length() > 0) {
      count++;
      output()
          .port("count")
          .send(count, message::handle);
    }
    else {
      message.ack();
    }
  }

}
