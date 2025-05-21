package com.dream11.grpc;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

import com.dream11.grpc.injector.GuiceInjector;
import com.dream11.grpc.util.SharedDataUtil;
import com.dream11.grpc.verticle.GrpcVerticle;
import com.google.inject.Guice;
import io.vertx.core.DeploymentOptions;
import io.vertx.reactivex.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

@Slf4j
public class Setup
    implements BeforeAllCallback, AfterAllCallback, ExtensionContext.Store.CloseableResource {
  final Vertx vertx = Vertx.vertx();

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    this.vertx.rxClose().blockingAwait();
  }

  @Override
  public void beforeAll(ExtensionContext extensionContext) {
    GuiceInjector injector = new GuiceInjector(Guice.createInjector());
    SharedDataUtil.setInstance(vertx.getDelegate(), injector);
    final String verticleName = GrpcVerticle.class.getName();
    String __ =
        this.vertx
            .rxDeployVerticle(
                injector.getInstance(GrpcVerticle.class), new DeploymentOptions().setInstances(1))
            .doOnError(error -> log.error("Error in deploying verticle : {}", verticleName, error))
            .doOnSuccess(v -> log.info("Deployed verticle : {}", verticleName))
            .blockingGet();
    extensionContext.getRoot().getStore(GLOBAL).put("test", this);
  }

  @Override
  public void close() {}
}
