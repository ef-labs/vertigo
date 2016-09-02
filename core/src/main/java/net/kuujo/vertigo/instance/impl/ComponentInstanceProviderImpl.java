package net.kuujo.vertigo.instance.impl;

import io.vertx.core.Future;
import io.vertx.core.ServiceHelper;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.VertigoException;
import net.kuujo.vertigo.VertigoOptions;
import net.kuujo.vertigo.context.ComponentContext;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.deployment.DeploymentManager;
import net.kuujo.vertigo.instance.ComponentInstance;
import net.kuujo.vertigo.spi.ComponentInstanceProvider;
import net.kuujo.vertigo.spi.ComponentInstanceFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class ComponentInstanceProviderImpl implements ComponentInstanceProvider {

  ComponentInstanceFactory factory = ServiceHelper.loadFactory(ComponentInstanceFactory.class);

  @Override
  public void createInstance(Vertx vertx, JsonObject config, Future<ComponentInstance> componentFuture) {

    DeploymentManager manager = DeploymentManager.manager(vertx, new VertigoOptions());

    String id = config.getString("vertigo_component_id");
    String networkId = config.getString("vertigo_network_id");

    manager.getNetwork(networkId, result -> {
      if (result.succeeded()) {
        NetworkContext network = result.result();

        ComponentContext cc = network.component(id);
        if (cc == null) {
          componentFuture.fail(new VertigoException("ComponentContext " + id + " does not exist in the network"));
          return;
        }

        ComponentInstance component = factory.createComponentInstance(vertx, cc);
        component.start(result2 -> {
          if (result2.succeeded()) {
            componentFuture.complete(component);
          } else {
            componentFuture.fail(result2.cause());
          }
        });
      } else {
        componentFuture.fail(result.cause());
      }
    });

  }
}
