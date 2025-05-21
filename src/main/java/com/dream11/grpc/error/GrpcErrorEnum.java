package com.dream11.grpc.error;

import com.google.rpc.Code;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration of standard gRPC errors used across the application. This enum implements {@link
 * GrpcError} to provide a set of predefined error types that can be used in gRPC services.
 *
 * <p>Each enum value represents a specific error condition with:
 *
 * <ul>
 *   <li>A unique error code
 *   <li>A descriptive error message
 *   <li>A corresponding gRPC status code
 * </ul>
 *
 * <p>This enum can be extended with additional error types as needed by the application. When
 * adding new error types, ensure that the error codes are unique and the gRPC status codes
 * accurately reflect the nature of the error.
 */
@Getter
@RequiredArgsConstructor
public enum GrpcErrorEnum implements GrpcError {
  /**
   * Represents an unknown or unexpected error that occurred during request processing. This is
   * typically used as a fallback when a more specific error type is not applicable.
   */
  UNKNOWN_EXCEPTION("UNKNOWN_EXCEPTION", "Something went wrong", Code.UNKNOWN);

  /** The unique identifier for this error type. */
  final String errorCode;

  /** A human-readable description of the error. */
  final String errorMessage;

  /** The corresponding gRPC status code for this error. */
  final Code grpcCode;
}
