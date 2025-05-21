package com.dream11.grpc.verticle;

import com.dream11.grpc.AbstractGrpcVerticle;
import com.dream11.grpc.ClassInjector;
import com.dream11.grpc.Constants;
import com.dream11.grpc.injector.GuiceInjector;
import com.dream11.grpc.util.SharedDataUtil;
import io.vertx.core.http.HttpServerOptions;

public class GrpcVerticle extends AbstractGrpcVerticle {
  protected GrpcVerticle() {
    super(Constants.TEST_PACKAGE_NAME, new HttpServerOptions().setPort(8080));
  }

  @Override
  protected ClassInjector getInjector() {
    return SharedDataUtil.getInstance(this.vertx.getDelegate(), GuiceInjector.class);
  }
}
