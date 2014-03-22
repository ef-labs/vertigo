/*
 * Copyright 2014 the original author or authors.
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
package net.kuujo.vertigo.test.integration;

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertFalse;
import static org.vertx.testtools.VertxAssert.assertNull;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.fail;
import static org.vertx.testtools.VertxAssert.testComplete;
import net.kuujo.vertigo.cluster.ClusterClient;
import net.kuujo.vertigo.cluster.ClusterEvent;
import net.kuujo.vertigo.cluster.RemoteClusterClient;
import net.kuujo.xync.test.integration.XyncTestVerticle;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * A remote cluster test.
 *
 * @author Jordan Halterman
 */
public class RemoteClusterClientTest extends XyncTestVerticle {

  public static class TestVerticle1 extends Verticle {
    @Override
    public void start() {
      super.start();
    }
  }

  public static class TestVerticle2 extends Verticle {
    @Override
    public void start() {
      testComplete();
    }
  }

  @Test
  public void testDeployVerticle() {
    final ClusterClient client = new RemoteClusterClient(vertx);
    client.deployVerticle("test-verticle1", TestVerticle2.class.getName(), new JsonObject().putString("foo", "bar"), 1, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        assertEquals("test-verticle1", result.result());
      }
    });
  }

  @Test
  public void testUndeployVerticle() {
    final ClusterClient client = new RemoteClusterClient(vertx);
    client.deployVerticle("test-verticle2", TestVerticle1.class.getName(), new JsonObject().putString("foo", "bar"), 1, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        assertEquals("test-verticle2", result.result());
        client.undeployVerticle("test-verticle2", new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            assertTrue(result.succeeded());
            testComplete();
          }
        });
      }
    });
  }

  @Test
  public void testVerticleIsDeployed() {
    final ClusterClient client = new RemoteClusterClient(vertx);
    client.deployVerticle("test-verticle3", TestVerticle1.class.getName(), new JsonObject().putString("foo", "bar"), 1, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        assertEquals("test-verticle3", result.result());
        client.isDeployed("test-verticle3", new Handler<AsyncResult<Boolean>>() {
          @Override
          public void handle(AsyncResult<Boolean> result) {
            assertTrue(result.succeeded());
            assertTrue(result.result());
            testComplete();
          }
        });
      }
    });
  }

  @Test
  public void testVerticleIsNotDeployed() {
    final ClusterClient client = new RemoteClusterClient(vertx);
    client.deployVerticle("test-verticle4", TestVerticle1.class.getName(), new JsonObject().putString("foo", "bar"), 1, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        assertEquals("test-verticle4", result.result());
        client.undeployVerticle("test-verticle4", new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            assertTrue(result.succeeded());
            client.isDeployed("test-verticle4", new Handler<AsyncResult<Boolean>>() {
              @Override
              public void handle(AsyncResult<Boolean> result) {
                assertTrue(result.succeeded());
                assertFalse(result.result());
                testComplete();
              }
            });
          }
        });
      }
    });
  }

  @Test
  public void testDeployWorkerVerticle() {
    final ClusterClient client = new RemoteClusterClient(vertx);
    client.deployWorkerVerticle("test-worker1", TestVerticle2.class.getName(), new JsonObject().putString("foo", "bar"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        assertEquals("test-worker1", result.result());
      }
    });
  }

  @Test
  public void testUndeployWorkerVerticle() {
    final ClusterClient client = new RemoteClusterClient(vertx);
    client.deployWorkerVerticle("test-worker2", TestVerticle1.class.getName(), new JsonObject().putString("foo", "bar"), 1, false, new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
        assertEquals("test-worker2", result.result());
        client.undeployVerticle("test-worker2", new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            assertTrue(result.succeeded());
            testComplete();
          }
        });
      }
    });
  }

  @Test
  public void testSetGetDelete() {
    final ClusterClient client = new RemoteClusterClient(vertx);
    client.set("foo", "bar", new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        assertTrue(result.succeeded());
        client.get("foo", new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            assertTrue(result.succeeded());
            assertEquals("bar", result.result());
            client.delete("foo", new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                assertTrue(result.succeeded());
                client.get("foo", new Handler<AsyncResult<String>>() {
                  @Override
                  public void handle(AsyncResult<String> result) {
                    assertTrue(result.succeeded());
                    assertNull(result.result());
                    testComplete();
                  }
                });
              }
            });
          }
        });
      }
    });
  }

  @Test
  public void testWatchCreate() {
    final ClusterClient cluster = new RemoteClusterClient(vertx);
    cluster.watch("test1", new Handler<ClusterEvent>() {
      @Override
      public void handle(ClusterEvent event) {
        if (event.type().equals(ClusterEvent.Type.CREATE)) {
          assertEquals(ClusterEvent.Type.CREATE, event.type());
          assertEquals("test1", event.key());
          assertEquals("Hello world 1!", event.value());
          testComplete();
        }
      }
    }, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          fail(result.cause().getMessage());
        }
        else {
          cluster.set("test1", "Hello world 1!");
        }
      }
    });
  }

  @Test
  public void testWatchUpdate() {
    final ClusterClient cluster = new RemoteClusterClient(vertx);
    cluster.watch("test2", new Handler<ClusterEvent>() {
      @Override
      public void handle(ClusterEvent event) {
        if (event.type().equals(ClusterEvent.Type.UPDATE)) {
          assertEquals(ClusterEvent.Type.UPDATE, event.type());
          assertEquals("test2", event.key());
          assertEquals("Hello world 2 again!", event.value());
          testComplete();
        }
      }
    }, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          fail(result.cause().getMessage());
        }
        else {
          cluster.set("test2", "Hello world 2!", new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                fail(result.cause().getMessage());
              }
              else {
                cluster.set("test2", "Hello world 2 again!");
              }
            }
          });
        }
      }
    });
  }

  @Test
  public void testWatchDelete() {
    final ClusterClient cluster = new RemoteClusterClient(vertx);
    cluster.watch("test3", new Handler<ClusterEvent>() {
      @Override
      public void handle(ClusterEvent event) {
        if (event.type().equals(ClusterEvent.Type.DELETE)) {
          assertEquals(ClusterEvent.Type.DELETE, event.type());
          assertEquals("test3", event.key());
          assertEquals("Hello world 3!", event.value());
          testComplete();
        }
      }
    }, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          fail(result.cause().getMessage());
        }
        else {
          cluster.set("test3", "Hello world 3!", new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                fail(result.cause().getMessage());
              }
              else {
                cluster.delete("test3");
              }
            }
          });
        }
      }
    });
  }

}
