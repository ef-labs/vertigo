package net.kuujo.vertigo.integration;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.test.core.VertxTestBase;
import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.component.AbstractComponent;
import net.kuujo.vertigo.network.NetworkConfig;
import net.kuujo.vertigo.network.builder.NetworkBuilder;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Deployment_Undeploy_Test extends VertxTestBase {

  private static Logger logger = LoggerFactory.getLogger(VertigoTestBase.class.getName());
  protected static AtomicInteger deploymentCounter = new AtomicInteger();
  protected static AtomicInteger undeploymentCounter = new AtomicInteger();

  @Override
  public void setUp() throws Exception {
    super.setUp();

    NetworkBuilder builder = NetworkConfig.builder("Network_Undeploy_Test");
    builder
        .connect("start").identifier(TestComponent.class.getName()).port("out")
        .to("end").identifier(TestComponent.class.getName()).port("in");
    NetworkConfig network = builder.build();

    // Deploy network
    CountDownLatch latch = new CountDownLatch(1);

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
    CountDownLatch undeployNetworkLatch = new CountDownLatch(1);
    vertx.runOnContext(aVoid -> {
      Vertigo vertigo = Vertigo.vertigo(vertx);
      vertigo.undeployNetwork("Network_Undeploy_Test", result -> {
        if (result.failed()) {
          fail(result.cause().getMessage());
        }
        undeployNetworkLatch.countDown();
      });
    });
    undeployNetworkLatch.await();

    assertEquals(2, deploymentCounter.get());
    assertEquals(2, undeploymentCounter.get());

    testComplete();
    await();
  }

  protected static Logger logger() {
    return logger;
  }

  public static class TestComponent extends AbstractComponent {
    String name;
    @Override
    public void start() throws Exception {
      name = component().context().address();
      logger().info(name + ": starting.");
      deploymentCounter.incrementAndGet();
    }

    @Override
    public void stop() throws Exception {
      logger().info(name + ": stopping.");
      undeploymentCounter.incrementAndGet();
    }
  }

}
