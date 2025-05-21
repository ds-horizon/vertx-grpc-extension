package com.dream11.grpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as a gRPC service. Classes annotated with this annotation will be
 * automatically discovered and registered by the {@link com.dream11.grpc.AbstractGrpcVerticle} when
 * it starts.
 *
 * <p>The annotated class should implement a gRPC service interface generated from a .proto file.
 */
@Target({ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface GrpcService {}
