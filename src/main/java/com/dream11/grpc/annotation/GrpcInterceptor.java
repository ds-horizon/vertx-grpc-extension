package com.dream11.grpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as a gRPC server interceptor. Classes annotated with this annotation
 * will be automatically discovered and registered by the {@link
 * com.dream11.grpc.AbstractGrpcVerticle} when it starts.
 *
 * <p>The annotated class should implement {@link io.grpc.ServerInterceptor} and provide the
 * interceptor logic in the {@link io.grpc.ServerInterceptor#interceptCall} method.
 */
@Target({ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface GrpcInterceptor {}
