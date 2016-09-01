package net.kuujo.vertigo.integration;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.test.core.VertxTestBase;
import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.network.builder.NetworkBuilder;
import net.kuujo.vertigo.component.AbstractComponent;
import net.kuujo.vertigo.network.NetworkConfig;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

public class Deployment_Deploy_Test extends VertxTestBase {

  private static Logger logger = LoggerFactory.getLogger(VertigoTestBase.class.getName());
  protected static CompletableFuture<Void> componentDeployed;

  @Test
  public void test() throws Exception {

    NetworkBuilder builder = NetworkConfig.builder();
    builder
        .connect("start").identifier(TestComponent.class.getName()).port("out")
        .to("end").identifier(TestComponent.class.getName()).port("in");
    NetworkConfig network = builder.build();

    // Deploy network
    CompletableFuture<Void> networkDeployedFuture = new CompletableFuture<>();
    componentDeployed = new CompletableFuture<>();

    vertx.runOnContext(aVoid -> {
      Vertigo vertigo = Vertigo.vertigo(vertx);
      vertigo.deployNetwork(network, result -> {
        if (result.failed()) {
          fail(result.cause().getMessage());
        }
        networkDeployedFuture.complete(null);
      });
    });

    componentDeployed.join();
    networkDeployedFuture.join();

    testComplete();
  }

  protected static Logger logger() {
    return logger;
  }

  public static class TestComponent extends AbstractComponent {
    @Override
    public void start() throws Exception {
      componentDeployed.complete(null);
    }
  }

}
