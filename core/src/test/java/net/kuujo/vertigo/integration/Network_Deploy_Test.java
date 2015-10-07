package net.kuujo.vertigo.integration;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.test.core.VertxTestBase;
import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.builder.NetworkBuilder;
import net.kuujo.vertigo.component.AbstractComponent;
import net.kuujo.vertigo.network.Network;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class Network_Deploy_Test extends VertxTestBase {

  private static Logger logger = LoggerFactory.getLogger(VertigoTestBase.class.getName());
  protected static CountDownLatch deploymentCounter;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    NetworkBuilder builder = Network.builder();
    builder
        .connect("start").identifier(TestComponent.class.getName()).port("out")
        .to("end").identifier(TestComponent.class.getName()).port("in");
    Network network = builder.build();

    // Deploy network
    CountDownLatch latch = new CountDownLatch(1);
    deploymentCounter = new CountDownLatch(2);

    vertx.runOnContext(aVoid -> {
      Vertigo vertigo = Vertigo.vertigo(vertx);
      vertigo.deployNetwork(network, result -> {
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
    deploymentCounter.await();
    testComplete();
  }

  protected static Logger logger() {
    return logger;
  }

  public static class TestComponent extends AbstractComponent {
    @Override
    public void start() throws Exception {
      deploymentCounter.countDown();
    }
  }

}
