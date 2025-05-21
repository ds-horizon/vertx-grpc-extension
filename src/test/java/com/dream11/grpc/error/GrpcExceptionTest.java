package com.dream11.grpc.error;

import static org.assertj.core.api.Assertions.assertThat;

import com.dream11.grpc.GrpcErrorTestEnum;
import org.junit.jupiter.api.Test;

class GrpcExceptionTest {
  @Test
  void testGrpcException() {
    // arrange
    String errorMessage = "An error has occurred";
    Throwable cause = new RuntimeException(errorMessage);
    GrpcError grpcError = GrpcErrorTestEnum.UNKNOWN_EXCEPTION;

    // act
    GrpcException grpcException = new GrpcException(grpcError);
    GrpcException grpcExceptionWithCause = new GrpcException(grpcError, cause);

    // assert
    assertThat(grpcException.getGrpcCode()).isEqualTo(grpcError.getGrpcCode());
    assertThat(grpcException.getErrorCode()).isEqualTo(grpcError.getErrorCode());
    assertThat(grpcException.getMessage()).isEqualTo(grpcError.getErrorMessage());
    assertThat(grpcException.getCause()).isNull();
    assertThat(grpcExceptionWithCause.getGrpcCode()).isEqualTo(grpcError.getGrpcCode());
    assertThat(grpcExceptionWithCause.getErrorCode()).isEqualTo(grpcError.getErrorCode());
    assertThat(grpcExceptionWithCause.getMessage()).isEqualTo(grpcError.getErrorMessage());
    assertThat(grpcExceptionWithCause.getCause()).isEqualTo(cause);
  }
}
