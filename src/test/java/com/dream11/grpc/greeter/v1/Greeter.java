package com.dream11.grpc.greeter.v1;

import com.dream11.grpc.annotation.GrpcService;
import io.reactivex.Single;

@GrpcService
public class Greeter extends RxGreeterGrpc.GreeterImplBase {

  @Override
  public Single<HelloReply> sayHello(HelloRequest request) {
    return Single.just(HelloReply.newBuilder().setMessage("Hello " + request.getName()).build());
  }
}
