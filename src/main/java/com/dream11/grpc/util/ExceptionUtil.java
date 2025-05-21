package com.dream11.grpc.util;

import com.dream11.grpc.error.GrpcError;
import com.dream11.grpc.error.GrpcErrorEnum;
import com.dream11.grpc.error.GrpcException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ExceptionUtil {
  public Throwable parseThrowable(final Throwable throwable) {
    if (throwable instanceof GrpcException) {
      return ((GrpcException) throwable).toStatusRuntimeException();
    } else {
      return new GrpcException(GrpcErrorEnum.UNKNOWN_EXCEPTION, throwable)
          .toStatusRuntimeException();
    }
  }

  public GrpcException getException(GrpcError grpcError, Object... params) {
    String message = String.format(grpcError.getErrorMessage(), params);
    return new GrpcException(grpcError.getErrorCode(), message, grpcError.getGrpcCode());
  }
}
