package com.dream11.grpc.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.dream11.grpc.GrpcErrorTestEnum;
import com.dream11.grpc.error.GrpcError;
import com.dream11.grpc.error.GrpcException;
import com.google.rpc.Code;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;

class ExceptionUtilTest {

  @Test
  void testParseThrowableGenericException() {
    // arrange
    String errorMessage = "Something went wrong";
    Throwable throwable = new RuntimeException(errorMessage);

    // act
    StatusRuntimeException parsedThrowable =
        (StatusRuntimeException) ExceptionUtil.parseThrowable(throwable);

    // assert
    assertThat(parsedThrowable).isInstanceOf(StatusRuntimeException.class);
    assertThat(parsedThrowable.getStatus().getCode().value()).isEqualTo(Code.UNKNOWN_VALUE);
    assertThat(parsedThrowable.getStatus().getDescription()).isEqualTo(errorMessage);
  }

  @Test
  void testParseThrowableGrpcException() {
    // arrange
    String errorMessage = "An error has occurred";
    Throwable throwable = new GrpcException("ERROR", errorMessage, Code.INVALID_ARGUMENT);

    // act
    StatusRuntimeException parsedThrowable =
        (StatusRuntimeException) ExceptionUtil.parseThrowable(throwable);

    // assert
    assertThat(parsedThrowable).isInstanceOf(StatusRuntimeException.class);
    assertThat(parsedThrowable.getStatus().getCode().value())
        .isEqualTo(Code.INVALID_ARGUMENT_VALUE);
    assertThat(parsedThrowable.getStatus().getDescription()).isEqualTo(errorMessage);
  }

  @Test
  void testGetException() {
    // arrange
    String errorMessage = "Error:Something went wrong, message:Error message";
    GrpcError grpcError = GrpcErrorTestEnum.UNKNOWN_EXCEPTION;

    // act
    GrpcException grpcException =
        ExceptionUtil.getException(grpcError, "Something went wrong", "Error message");

    // assert
    assertThat(grpcException.getGrpcCode()).isEqualTo(grpcError.getGrpcCode());
    assertThat(grpcException.getErrorCode()).isEqualTo(grpcError.getErrorCode());
    assertThat(grpcException.getMessage()).isEqualTo(errorMessage);
    assertThat(grpcException.getMessage()).isEqualTo(grpcException.getErrorMessage());
  }
}
