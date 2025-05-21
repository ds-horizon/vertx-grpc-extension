package com.dream11.grpc;

/**
 * Interface for dependency injection in gRPC services and interceptors. Implementations of this
 * interface should provide a mechanism to create and manage instances of service and interceptor
 * classes.
 */
public interface ClassInjector {
  /**
   * Returns an instance of the specified class, typically created through dependency injection.
   * This method is used by the gRPC server to create service and interceptor instances.
   *
   * @param <T> The type of instance to create
   * @param clazz The class for which to create an instance
   * @return An instance of the specified class
   */
  <T> T getInstance(Class<T> clazz);
}
