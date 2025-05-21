# Vert.x gRPC Utilities

A utility library that simplifies the development of gRPC services using Vert.x. This library provides a set of tools and abstractions to make it easier to create, test, and maintain gRPC services in a Vert.x environment.

## Features

- **Easy Service Registration**: Automatically discover and register gRPC services using annotations
- **Dependency Injection**: Flexible dependency injection system for services and interceptors
- **Standardized Error Handling**: Comprehensive error handling system with gRPC status integration
- **Built-in Logging**: Request/response logging interceptor for debugging and monitoring
- **Reflection Support**: Optional gRPC reflection service for service discovery

## Getting Started

### Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.dream11</groupId>
    <artifactId>vertx-grpc-extension</artifactId>
    <version>x.y.z</version>
</dependency>
```

## Usage

### Creating a gRPC Service

1. Create your service implementation:

```java
@GrpcService
public class MyGrpcService extends MyGrpcServiceGrpc.MyGrpcServiceImplBase {
    @Override
    public void myMethod(MyRequest request, StreamObserver<MyResponse> responseObserver) {
        // Your implementation here
    }
}
```

2. Create an interceptor(optional):

```java
import com.dream11.grpc.annotation.GrpcInterceptor;

@GrpcInterceptor
public class MyInterceptor implements ServerInterceptor {
  @Override
  public <R1, R2> ServerCall.Listener<R1> interceptCall(ServerCall<R1, R2> serverCall, Metadata metadata, ServerCallHandler<R1, R2> next) {
    // Your implementation here
  }
}
```

3. Create your Vertical:

```java
public class MyGrpcVerticle extends AbstractGrpcVerticle {
    public MyGrpcVertical() {
        super("com.your.package");
    }

    @Override
    protected ClassInjector getInjector() {
        // Return your dependency injector implementation
        return new MyClassInjector();
    }
}
```

### Error Handling

The library provides a standardized way to handle errors in gRPC services:

- Implement `GrpcError` as an enum to specify error codes, messages and grpc status codes
- Throw `GrpcException` with the enum implementing `GrpcError` to return a gRPC error response
