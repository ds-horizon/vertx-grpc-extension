package com.dream11.grpc;

import com.dream11.grpc.annotation.GrpcInterceptor;
import com.dream11.grpc.annotation.GrpcService;
import com.dream11.grpc.interceptor.LoggingInterceptor;
import com.dream11.grpc.util.AnnotationUtil;
import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.reactivex.Completable;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.grpc.server.GrpcServiceBridge;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.grpc.server.GrpcServer;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class for creating and managing gRPC servers in a Vert.x environment. This class
 * handles the setup and lifecycle of a gRPC server, including:
 *
 * <ul>
 *   <li>Server creation and configuration
 *   <li>Service registration with interceptors
 *   <li>Reflection service support
 *   <li>Automatic discovery of services and interceptors using annotations
 * </ul>
 *
 * To use this class, extend it and implement the {@link #getInjector()} method to provide
 * dependency injection for your services and interceptors.
 */
@Slf4j
public abstract class AbstractGrpcVerticle extends AbstractVerticle {

  private final String packageName;
  private final boolean hasReflectionService;
  private HttpServer httpServer;
  private GrpcServer grpcServer;
  final HttpServerOptions httpServerOptions;

  /**
   * Creates a new gRPC verticle with reflection service enabled.
   *
   * @param packageName The package name to scan for {@link GrpcService} and {@link GrpcInterceptor}
   *     annotations
   */
  protected AbstractGrpcVerticle(String packageName) {
    this(packageName, true);
  }

  /**
   * Creates a new gRPC verticle with configurable reflection service.
   *
   * @param packageName The package name to scan for {@link GrpcService} and {@link GrpcInterceptor}
   *     annotations
   * @param hasReflectionService Whether to enable the gRPC reflection service
   */
  protected AbstractGrpcVerticle(String packageName, boolean hasReflectionService) {
    this(packageName, new HttpServerOptions(), hasReflectionService);
  }

  /**
   * Creates a new gRPC verticle with configurable reflection service.
   *
   * @param packageName The package name to scan for {@link GrpcService} and {@link GrpcInterceptor}
   *     annotations
   * @param httpServerOptions Custom HTTP server options
   */
  protected AbstractGrpcVerticle(String packageName, HttpServerOptions httpServerOptions) {
    this(packageName, httpServerOptions, true);
  }

  /**
   * Creates a new gRPC verticle with full configuration options.
   *
   * @param packageName The package name to scan for {@link GrpcService} and {@link GrpcInterceptor}
   *     annotations
   * @param httpServerOptions Custom HTTP server options
   * @param hasReflectionService Whether to enable the gRPC reflection service
   */
  protected AbstractGrpcVerticle(
      String packageName, HttpServerOptions httpServerOptions, boolean hasReflectionService) {
    this.packageName = packageName;
    this.hasReflectionService = hasReflectionService;
    this.httpServerOptions = httpServerOptions;
  }

  /**
   * Provides the dependency injector for creating service and interceptor instances. This method
   * must be implemented by subclasses to provide the appropriate dependency injection mechanism.
   *
   * @return A {@link ClassInjector} instance that can create service and interceptor instances
   */
  protected abstract ClassInjector getInjector();

  /**
   * Starts the gRPC server. This method is called by Vert.x when the verticle is deployed. It
   * initializes the server, registers services and interceptors, and starts listening for requests.
   *
   * @return A {@link Completable} that completes when the server is started successfully
   */
  @Override
  public Completable rxStart() {
    return this.rxStartGrpcServer();
  }

  /**
   * Internal method to start the gRPC server. This method:
   *
   * <ul>
   *   <li>Creates the HTTP and gRPC servers
   *   <li>Registers the reflection service if enabled
   *   <li>Discovers and registers all services with their interceptors
   *   <li>Starts the server listening for requests
   * </ul>
   *
   * @return A {@link Completable} that completes when the server is started successfully
   */
  @SneakyThrows
  protected Completable rxStartGrpcServer() {
    // Create gRPC server
    this.httpServer = this.vertx.createHttpServer(this.httpServerOptions);
    this.grpcServer = GrpcServer.server(this.vertx);
    List<ServerInterceptor> interceptors = this.getAllInterceptors();

    // Register reflection service
    if (this.hasReflectionService) {
      this.addServiceWithInterceptors(ProtoReflectionService.newInstance(), List.of());
    }

    // Register services
    for (Class<?> clazz : this.getGrpcServices()) {
      log.debug("Registering service:{}", clazz.getName());
      this.addServiceWithInterceptors(
          (BindableService) this.getInjector().getInstance(clazz), interceptors);
    }

    return this.httpServer
        .requestHandler(this.grpcServer)
        .rxListen()
        .ignoreElement()
        .doOnComplete(() -> log.info("gRPC server started successfully"))
        .doOnError(err -> log.info("Failed to start gRPC server", err));
  }

  /**
   * Registers a service with the specified interceptors.
   *
   * @param service The gRPC service to register
   * @param interceptors List of interceptors to apply to the service
   */
  private void addServiceWithInterceptors(
      BindableService service, List<ServerInterceptor> interceptors) {
    // Add all interceptors to service
    GrpcServiceBridge.bridge(ServerInterceptors.intercept(service, interceptors))
        .bind(this.grpcServer.getDelegate());
  }

  /**
   * Collects all interceptors that should be applied to services. This includes both the
   * request/response interceptor and any custom interceptors.
   *
   * @return List of server interceptors to apply
   */
  private List<ServerInterceptor> getAllInterceptors() {
    List<ServerInterceptor> interceptors = new ArrayList<>();
    // Register Request Response Interceptor
    if (this.getRequestResponseInterceptor() != null) {
      interceptors.add(this.getRequestResponseInterceptor());
    }
    // Register custom interceptors
    for (Class<?> clazz : this.getGrpcInterceptors()) {
      log.debug("Adding interceptor:{}", clazz.getName());
      interceptors.add((ServerInterceptor) this.getInjector().getInstance(clazz));
    }
    return interceptors;
  }

  /**
   * Discovers all classes annotated with {@link GrpcService} in the configured package.
   *
   * @return List of service classes to register
   */
  protected List<Class<?>> getGrpcServices() {
    return AnnotationUtil.getClassesWithAnnotation(this.packageName, GrpcService.class);
  }

  /**
   * Discovers all classes annotated with {@link GrpcInterceptor} in the configured package.
   *
   * @return List of interceptor classes to register
   */
  protected List<Class<?>> getGrpcInterceptors() {
    return AnnotationUtil.getClassesWithAnnotation(this.packageName, GrpcInterceptor.class);
  }

  /**
   * Provides the request/response logging interceptor. Override this method to provide a custom
   * logging interceptor or return null to disable logging.
   *
   * @return A {@link ServerInterceptor} for logging requests and responses, or null to disable
   *     logging
   */
  protected ServerInterceptor getRequestResponseInterceptor() {
    return new LoggingInterceptor();
  }

  /**
   * Stops the gRPC server. This method is called by Vert.x when the verticle is undeployed.
   *
   * @return A {@link Completable} that completes when the server is stopped successfully
   */
  @Override
  public Completable rxStop() {
    return this.httpServer
        .rxClose()
        .doOnComplete(() -> log.info("gRPC server stopped successfully"))
        .doOnError(err -> log.info("Failed to stop gRPC server", err));
  }
}
