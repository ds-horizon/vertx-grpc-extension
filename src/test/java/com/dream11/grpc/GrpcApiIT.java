package com.dream11.grpc;

import static org.assertj.core.api.Assertions.assertThat;

import com.dream11.grpc.greeter.v1.HelloReply;
import com.dream11.grpc.greeter.v1.HelloRequest;
import com.dream11.grpc.greeter.v1.RxGreeterGrpc;
import io.grpc.ManagedChannel;
import io.vertx.core.Vertx;
import io.vertx.grpc.VertxChannelBuilder;
import io.vertx.junit5.VertxExtension;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({VertxExtension.class, Setup.class})
@Slf4j
class GrpcApiIT {

  static ManagedChannel CHANNEL;
  static RxGreeterGrpc.RxGreeterStub STUB;

  @BeforeAll
  static void setup(Vertx vertx) {
    CHANNEL = VertxChannelBuilder.forAddress(vertx, "localhost", 8080).usePlaintext().build();
    STUB = RxGreeterGrpc.newRxStub(CHANNEL);
  }

  @Test
  void testSuccessRequest() {
    // Arrange & Act
    HelloReply response =
        STUB.sayHello(HelloRequest.newBuilder().setName("TESTS").build()).blockingGet();

    // Assert
    assertThat(response.getMessage()).isEqualTo("Hello TESTS");
  }
}
