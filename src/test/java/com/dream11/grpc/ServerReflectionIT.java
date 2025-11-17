package com.dream11.grpc;

import static org.assertj.core.api.Assertions.assertThat;

import io.grpc.ManagedChannel;
import io.grpc.reflection.v1alpha.ExtensionRequest;
import io.grpc.reflection.v1alpha.ServerReflectionGrpc;
import io.grpc.reflection.v1alpha.ServerReflectionRequest;
import io.grpc.reflection.v1alpha.ServerReflectionResponse;
import io.grpc.stub.StreamObserver;
import io.vertx.core.Vertx;
import io.vertx.grpc.VertxChannelBuilder;
import io.vertx.junit5.VertxExtension;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith({VertxExtension.class, Setup.class})
class ServerReflectionIT {

  static ManagedChannel CHANNEL;
  static ServerReflectionGrpc.ServerReflectionStub STUB;

  @BeforeAll
  static void setup(Vertx vertx) {
    CHANNEL = VertxChannelBuilder.forAddress(vertx, "localhost", 8080).usePlaintext().build();
    STUB = ServerReflectionGrpc.newStub(CHANNEL);
  }

  @Test
  @SneakyThrows
  void testList() {
    // Arrange
    CompletableFuture<ServerReflectionResponse> future = new CompletableFuture<>();
    StreamObserver<ServerReflectionRequest> streamObserver =
        STUB.serverReflectionInfo(new ResponseStreamObserver<>(future));

    // Act
    streamObserver.onNext(ServerReflectionRequest.newBuilder().setListServices("").build());

    // Assert
    ServerReflectionResponse reflectionResponse = future.get();
    assertThat(reflectionResponse.getListServicesResponse().getServiceCount()).isEqualTo(1);
    assertThat(reflectionResponse.getListServicesResponse().getService(0).getName())
        .isEqualTo("grpc.greeter.v1.Greeter");
  }

  @Test
  @SneakyThrows
  void testMessageRequestNotSet() {
    // Arrange
    CompletableFuture<ServerReflectionResponse> future = new CompletableFuture<>();
    StreamObserver<ServerReflectionRequest> streamObserver =
        STUB.serverReflectionInfo(new ResponseStreamObserver<>(future));

    // Act
    streamObserver.onNext(ServerReflectionRequest.newBuilder().build());

    // Assert
    ServerReflectionResponse reflectionResponse = future.get();
    assertThat(reflectionResponse.getErrorResponse().getErrorCode()).isEqualTo(12);
    assertThat(reflectionResponse.getErrorResponse().getErrorMessage())
        .isEqualTo("not implemented MESSAGEREQUEST_NOT_SET");
  }

  @SneakyThrows
  @ParameterizedTest
  @MethodSource("fileDescriptorSuccessRequest")
  void testFileDescriptorSuccess(ServerReflectionRequest reflectionRequest) {
    // Arrange
    CompletableFuture<ServerReflectionResponse> future = new CompletableFuture<>();
    StreamObserver<ServerReflectionRequest> streamObserver =
        STUB.serverReflectionInfo(new ResponseStreamObserver<>(future));

    // Act
    streamObserver.onNext(reflectionRequest);

    // Assert
    ServerReflectionResponse reflectionResponse = future.get();
    assertThat(reflectionResponse.getFileDescriptorResponse().getFileDescriptorProtoCount())
        .isEqualTo(2);
  }

  @SneakyThrows
  @ParameterizedTest
  @MethodSource("errorRequest")
  void testErrors(ServerReflectionRequest reflectionRequest, String message) {
    // Arrange
    CompletableFuture<ServerReflectionResponse> future = new CompletableFuture<>();
    StreamObserver<ServerReflectionRequest> streamObserver =
        STUB.serverReflectionInfo(new ResponseStreamObserver<>(future));

    // Act
    streamObserver.onNext(reflectionRequest);

    // Assert
    ServerReflectionResponse reflectionResponse = future.get();
    assertThat(reflectionResponse.getErrorResponse().getErrorCode()).isEqualTo(5);
    assertThat(reflectionResponse.getErrorResponse().getErrorMessage()).isEqualTo(message);
  }

  @Test
  @SneakyThrows
  void testAllExtensionNumbersOfType() {
    // Arrange
    String type = "google.protobuf.MethodOptions";
    List<Integer> expectedNumbers = List.of(50000);
    CompletableFuture<ServerReflectionResponse> future = new CompletableFuture<>();
    StreamObserver<ServerReflectionRequest> streamObserver =
        STUB.serverReflectionInfo(new ResponseStreamObserver<>(future));

    // Act
    streamObserver.onNext(
        ServerReflectionRequest.newBuilder().setAllExtensionNumbersOfType(type).build());

    // Assert
    ServerReflectionResponse reflectionResponse = future.get();
    assertThat(reflectionResponse.getAllExtensionNumbersResponse().getBaseTypeName())
        .isEqualTo(type);
    assertThat(reflectionResponse.getAllExtensionNumbersResponse().getExtensionNumberList())
        .containsExactlyElementsOf(expectedNumbers);
  }

  @Test
  @SneakyThrows
  void testAllExtensionNumbersOfTypeError() {
    // Arrange
    String type = "com.example.nonexistent";
    CompletableFuture<ServerReflectionResponse> future = new CompletableFuture<>();
    StreamObserver<ServerReflectionRequest> streamObserver =
        STUB.serverReflectionInfo(new ResponseStreamObserver<>(future));

    // Act
    streamObserver.onNext(
        ServerReflectionRequest.newBuilder().setAllExtensionNumbersOfType(type).build());

    // Assert
    ServerReflectionResponse reflectionResponse = future.get();
    assertThat(reflectionResponse.getErrorResponse().getErrorCode()).isEqualTo(5);
    assertThat(reflectionResponse.getErrorResponse().getErrorMessage())
        .isEqualTo("Type not found.");
  }

  private static Stream<Arguments> fileDescriptorSuccessRequest() {
    return Stream.of(
        Arguments.of(
            ServerReflectionRequest.newBuilder()
                .setFileContainingSymbol("grpc.greeter.v1.Greeter.SayHello")
                .build()),
        Arguments.of(
            ServerReflectionRequest.newBuilder()
                .setFileByFilename("grpc/greeter/v1/greeter.proto")
                .build()),
        Arguments.of(
            ServerReflectionRequest.newBuilder()
                .setFileContainingExtension(
                    ExtensionRequest.newBuilder()
                        .setContainingType("google.protobuf.MethodOptions")
                        .setExtensionNumber(50000)
                        .build())
                .build()));
  }

  private static Stream<Arguments> errorRequest() {
    return Stream.of(
        Arguments.of(
            ServerReflectionRequest.newBuilder()
                .setFileContainingSymbol("grpc.greeter.v1.Greeter.NonExistent")
                .build(),
            "Symbol not found (grpc.greeter.v1.Greeter.NonExistent)"),
        Arguments.of(
            ServerReflectionRequest.newBuilder()
                .setFileByFilename("grpc/greeter/v1/nonexistent.proto")
                .build(),
            "File not found (grpc/greeter/v1/nonexistent.proto)"),
        Arguments.of(
            ServerReflectionRequest.newBuilder()
                .setFileContainingExtension(
                    ExtensionRequest.newBuilder()
                        .setContainingType("google.protobuf.MethodOptions")
                        .setExtensionNumber(60000)
                        .build())
                .build(),
            "Extension not found (google.protobuf.MethodOptions, 60000)"));
  }

  private static class ResponseStreamObserver<T> implements StreamObserver<T> {

    CompletableFuture<T> future;

    ResponseStreamObserver(CompletableFuture<T> future) {
      this.future = future;
    }

    @Override
    public void onNext(T value) {
      future.complete(value);
    }

    @Override
    public void onError(Throwable t) {
      future.completeExceptionally(t);
    }

    @Override
    public void onCompleted() {}
  }
}
