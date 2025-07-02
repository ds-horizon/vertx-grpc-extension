# Vert.x gRPC Extension

A Library to provide a set of abstractions to make it easier to create and maintain gRPC services in Vert.x.

## Features

- **Easy Service & Interceptor Registration**: Automatically discover and register gRPC services and interceptors using annotations
- **Dependency Injection**: Flexible dependency injection system for services and interceptors
- **Standardized Error Handling**: Comprehensive error handling system with gRPC status integration
- **Built-in Logging**: Request/response logging interceptor for debugging and monitoring
- **Reflection Support**: Optional gRPC reflection service for service discovery

## Getting Started

### Installation

Add the following dependency to the `dependencies` section of your build descriptor:

- Maven (in your `pom.xml`):
```xml
  <dependency>
    <groupId>com.dream11</groupId>
    <artifactId>vertx-grpc-extension</artifactId>
    <version>x.y.z</version>
  </dependency>
```

- Gradle (in your `build.gradle` file):
```
  dependencies {
   compile 'com.dream11:vertx-grpc-extension:x.y.z'
  }
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

3. Create your Verticle:

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
