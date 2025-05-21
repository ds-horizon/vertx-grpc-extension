package com.dream11.grpc.interceptor;

import io.grpc.ForwardingServerCall;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;

/**
 * A gRPC server interceptor that logs request and response details for each RPC call. This
 * interceptor is used by default in {@link com.dream11.grpc.AbstractGrpcVerticle} to provide
 * request/response logging for all gRPC services.
 *
 * <p>
 */
@Slf4j
public class LoggingInterceptor implements ServerInterceptor {

  @Override
  public <R1, R2> ServerCall.Listener<R1> interceptCall(
      ServerCall<R1, R2> serverCall, Metadata metadata, ServerCallHandler<R1, R2> next) {
    log.debug("STATED METHOD: {}", serverCall.getMethodDescriptor().getFullMethodName());
    ServerCall<R1, R2> listener =
        new ForwardingServerCall.SimpleForwardingServerCall<>(serverCall) {

          @Override
          public void sendMessage(R2 message) {
            log.debug("Sending message to client: {}", message);
            super.sendMessage(message);
          }
        };

    return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(
        next.startCall(listener, metadata)) {

      @Override
      public void onMessage(R1 message) {
        log.debug("Received message from client: {}", message);
        super.onMessage(message);
      }
    };
  }
}
