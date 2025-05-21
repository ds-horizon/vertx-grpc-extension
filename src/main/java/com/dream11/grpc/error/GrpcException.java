package com.dream11.grpc.error;

import com.google.protobuf.Any;
import com.google.rpc.Code;
import com.google.rpc.ErrorInfo;
import com.google.rpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import lombok.Getter;

/**
 * Custom exception class for handling gRPC errors in a standardized way. This exception extends
 * {@link RuntimeException} and provides additional information about the error that can be
 * converted to a gRPC status response.
 *
 * <p>This exception class is designed to:
 *
 * <ul>
 *   <li>Wrap both predefined errors ({@link GrpcError}) and custom error information
 *   <li>Include detailed error information including error code, message, and gRPC status
 *   <li>Support cause chaining for tracking the original error
 *   <li>Convert to a gRPC {@link StatusRuntimeException} for client communication
 * </ul>
 *
 * <p>When thrown in a gRPC service, this exception can be caught and converted to a proper gRPC
 * status response using {@link #toStatusRuntimeException()}.
 */
@Getter
public class GrpcException extends RuntimeException {
  private final String errorCode;

  private final String errorMessage;

  private final Code grpcCode;

  /**
   * Creates a new gRPC exception from a predefined error type with an optional cause.
   *
   * @param grpcError The predefined error type
   * @param cause The original exception that caused this error, if any
   */
  public GrpcException(GrpcError grpcError, Throwable cause) {
    super(grpcError.getErrorMessage(), cause);
    this.errorCode = grpcError.getErrorCode();
    this.errorMessage = grpcError.getErrorMessage();
    this.grpcCode = grpcError.getGrpcCode();
  }

  /**
   * Creates a new gRPC exception with custom error information and an optional cause.
   *
   * @param errorCode The unique identifier for this error
   * @param errorMessage A human-readable description of the error
   * @param grpcCode The corresponding gRPC status code
   * @param cause The original exception that caused this error, if any
   */
  public GrpcException(String errorCode, String errorMessage, Code grpcCode, Throwable cause) {
    super(errorMessage, cause);
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
    this.grpcCode = grpcCode;
  }

  /**
   * Creates a new gRPC exception from a predefined error type without a cause.
   *
   * @param grpcError The predefined error type
   */
  public GrpcException(GrpcError grpcError) {
    this(grpcError, null);
  }

  /**
   * Creates a new gRPC exception with custom error information without a cause.
   *
   * @param errorCode The unique identifier for this error
   * @param errorMessage A human-readable description of the error
   * @param grpcCode The corresponding gRPC status code
   */
  public GrpcException(String errorCode, String errorMessage, Code grpcCode) {
    this(errorCode, errorMessage, grpcCode, null);
  }

  /**
   * Converts this exception to a gRPC {@link StatusRuntimeException} that can be returned to the
   * client. The status includes:
   *
   * <ul>
   *   <li>The gRPC status code
   *   <li>The error message
   *   <li>Additional error details including the error code and cause message
   * </ul>
   *
   * @return A {@link StatusRuntimeException} representing this error
   */
  public StatusRuntimeException toStatusRuntimeException() {
    String reason = this.getCause() == null ? this.getMessage() : this.getCause().getMessage();
    ErrorInfo info =
        ErrorInfo.newBuilder().setReason(reason).putMetadata("code", this.errorCode).build();

    Status status =
        Status.newBuilder()
            .setCode(this.grpcCode.getNumber())
            .setMessage(this.errorMessage)
            .addDetails(Any.pack(info))
            .build();
    return StatusProto.toStatusRuntimeException(status);
  }
}
