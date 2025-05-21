package com.dream11.grpc.error;

import com.google.rpc.Code;

/**
 * Interface defining the structure of gRPC errors in the application. This interface provides a
 * standardized way to represent errors that can be converted to gRPC status responses.
 *
 * <p>Implementations of this interface should provide:
 *
 * <ul>
 *   <li>A unique error code for identifying the specific error
 *   <li>A human-readable error message
 *   <li>The corresponding gRPC status code
 * </ul>
 *
 * <p>This interface is used by {@link GrpcException} to provide consistent error handling across
 * all gRPC services.
 */
public interface GrpcError {
  /**
   * Returns a unique identifier for this error type.
   *
   * @return A string representing the error code
   */
  String getErrorCode();

  /**
   * Returns a human-readable description of the error.
   *
   * @return A string containing the error message
   */
  String getErrorMessage();

  /**
   * Returns the corresponding gRPC status code for this error.
   *
   * @return A {@link Code} enum value representing the gRPC status
   */
  Code getGrpcCode();
}
