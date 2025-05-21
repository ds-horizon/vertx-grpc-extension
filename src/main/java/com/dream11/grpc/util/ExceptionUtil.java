package com.dream11.grpc.util;

import com.dream11.grpc.error.GrpcError;
import com.dream11.grpc.error.GrpcErrorEnum;
import com.dream11.grpc.error.GrpcException;
import lombok.experimental.UtilityClass;

/**
 * Utility class for handling and processing gRPC exceptions. This class provides methods to convert
 * and format exceptions for gRPC error handling.
 */
@UtilityClass
public class ExceptionUtil {
  /**
   * Parses a throwable and converts it to an appropriate gRPC status runtime exception. If the
   * throwable is already a GrpcException, it converts it to a StatusRuntimeException. Otherwise, it
   * wraps the throwable in a new GrpcException with UNKNOWN_EXCEPTION error code.
   *
   * @param throwable The throwable to parse and convert
   * @return A StatusRuntimeException representing the error
   */
  public Throwable parseThrowable(final Throwable throwable) {
    if (throwable instanceof GrpcException) {
      return ((GrpcException) throwable).toStatusRuntimeException();
    } else {
      return new GrpcException(GrpcErrorEnum.UNKNOWN_EXCEPTION, throwable)
          .toStatusRuntimeException();
    }
  }

  /**
   * Creates a new GrpcException with the specified error details and formatted message.
   *
   * @param grpcError The GrpcError containing error code, message template, and gRPC status code
   * @param params The parameters to format the error message with
   * @return A new GrpcException instance with the formatted message
   */
  public GrpcException getException(GrpcError grpcError, Object... params) {
    String message = String.format(grpcError.getErrorMessage(), params);
    return new GrpcException(grpcError.getErrorCode(), message, grpcError.getGrpcCode());
  }
}
