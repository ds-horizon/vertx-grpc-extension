package com.dream11.grpc.reflection;

import com.google.protobuf.Descriptors;
import io.grpc.Status;
import io.grpc.reflection.v1alpha.ErrorResponse;
import io.grpc.reflection.v1alpha.ExtensionNumberResponse;
import io.grpc.reflection.v1alpha.ExtensionRequest;
import io.grpc.reflection.v1alpha.FileDescriptorResponse;
import io.grpc.reflection.v1alpha.ListServiceResponse;
import io.grpc.reflection.v1alpha.ServerReflectionRequest;
import io.grpc.reflection.v1alpha.ServerReflectionResponse;
import io.grpc.reflection.v1alpha.ServiceResponse;
import io.vertx.core.Handler;
import io.vertx.grpc.server.GrpcServerRequest;
import io.vertx.grpc.server.GrpcServerResponse;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class ReflectionServiceV1Handler
    implements Handler<GrpcServerRequest<ServerReflectionRequest, ServerReflectionResponse>> {

  private final GrpcServerIndex index;

  public ReflectionServiceV1Handler(GrpcServerIndex index) {
    this.index = index;
  }

  @Override
  public void handle(GrpcServerRequest<ServerReflectionRequest, ServerReflectionResponse> request) {
    request.handler(
        serverReflectionRequest -> {
          GrpcServerResponse<ServerReflectionRequest, ServerReflectionResponse> response =
              request.response();
          switch (serverReflectionRequest.getMessageRequestCase()) {
            case LIST_SERVICES:
              response.end(this.getServiceList(serverReflectionRequest));
              break;
            case FILE_BY_FILENAME:
              response.end(this.getFileByName(serverReflectionRequest));
              break;
            case FILE_CONTAINING_SYMBOL:
              response.end(this.getFileContainingSymbol(serverReflectionRequest));
              break;
            case FILE_CONTAINING_EXTENSION:
              response.end(this.getFileByExtension(serverReflectionRequest));
              break;
            case ALL_EXTENSION_NUMBERS_OF_TYPE:
              response.end(this.getAllExtensions(serverReflectionRequest));
              break;
            default:
              response.end(
                  this.getErrorResponse(
                      serverReflectionRequest,
                      Status.Code.UNIMPLEMENTED,
                      "not implemented " + serverReflectionRequest.getMessageRequestCase()));
          }
        });
  }

  private ServerReflectionResponse getServiceList(ServerReflectionRequest request) {
    ListServiceResponse response =
        ListServiceResponse.newBuilder()
            .addAllService(
                this.index.getServiceNames().stream()
                    .map(s -> ServiceResponse.newBuilder().setName(s).build())
                    .collect(Collectors.toList()))
            .build();

    return ServerReflectionResponse.newBuilder()
        .setValidHost(request.getHost())
        .setOriginalRequest(request)
        .setListServicesResponse(response)
        .build();
  }

  private ServerReflectionResponse getFileByName(ServerReflectionRequest request) {
    String name = request.getFileByFilename();
    Descriptors.FileDescriptor fd = this.index.getFileDescriptorByName(name);
    if (fd != null) {
      return this.getServerReflectionResponse(request, fd);
    } else {
      return this.getErrorResponse(request, Status.Code.NOT_FOUND, "File not found (" + name + ")");
    }
  }

  private ServerReflectionResponse getFileContainingSymbol(ServerReflectionRequest request) {
    String symbol = request.getFileContainingSymbol();
    Descriptors.FileDescriptor fd = this.index.getFileDescriptorBySymbol(symbol);
    if (fd != null) {
      return this.getServerReflectionResponse(request, fd);
    } else {
      return this.getErrorResponse(
          request, Status.Code.NOT_FOUND, "Symbol not found (" + symbol + ")");
    }
  }

  private ServerReflectionResponse getFileByExtension(ServerReflectionRequest request) {
    ExtensionRequest extensionRequest = request.getFileContainingExtension();
    String type = extensionRequest.getContainingType();
    int extension = extensionRequest.getExtensionNumber();
    Descriptors.FileDescriptor fd =
        this.index.getFileDescriptorByExtensionAndNumber(type, extension);
    if (fd != null) {
      return this.getServerReflectionResponse(request, fd);
    } else {
      return this.getErrorResponse(
          request, Status.Code.NOT_FOUND, "Extension not found (" + type + ", " + extension + ")");
    }
  }

  private ServerReflectionResponse getAllExtensions(ServerReflectionRequest request) {
    String type = request.getAllExtensionNumbersOfType();
    Set<Integer> extensions = this.index.getExtensionNumbersOfType(type);
    if (!extensions.isEmpty()) {
      ExtensionNumberResponse.Builder builder =
          ExtensionNumberResponse.newBuilder()
              .setBaseTypeName(type)
              .addAllExtensionNumber(extensions);
      return ServerReflectionResponse.newBuilder()
          .setValidHost(request.getHost())
          .setOriginalRequest(request)
          .setAllExtensionNumbersResponse(builder)
          .build();
    } else {
      return this.getErrorResponse(request, Status.Code.NOT_FOUND, "Type not found.");
    }
  }

  private ServerReflectionResponse getServerReflectionResponse(
      ServerReflectionRequest request, Descriptors.FileDescriptor fd) {
    FileDescriptorResponse.Builder fdRBuilder = FileDescriptorResponse.newBuilder();

    // Traverse the descriptors to get the full list of dependencies and add them to the builder
    Set<String> seenFiles = new HashSet<>();
    Queue<Descriptors.FileDescriptor> frontier = new ArrayDeque<>();
    seenFiles.add(fd.getName());
    frontier.add(fd);
    while (!frontier.isEmpty()) {
      Descriptors.FileDescriptor nextFd = frontier.remove();
      fdRBuilder.addFileDescriptorProto(nextFd.toProto().toByteString());
      for (Descriptors.FileDescriptor dependencyFd : nextFd.getDependencies()) {
        if (!seenFiles.contains(dependencyFd.getName())) {
          seenFiles.add(dependencyFd.getName());
          frontier.add(dependencyFd);
        }
      }
    }
    return ServerReflectionResponse.newBuilder()
        .setValidHost(request.getHost())
        .setOriginalRequest(request)
        .setFileDescriptorResponse(fdRBuilder)
        .build();
  }

  private ServerReflectionResponse getErrorResponse(
      ServerReflectionRequest request, Status.Code code, String message) {
    return ServerReflectionResponse.newBuilder()
        .setValidHost(request.getHost())
        .setOriginalRequest(request)
        .setErrorResponse(
            ErrorResponse.newBuilder().setErrorCode(code.value()).setErrorMessage(message))
        .build();
  }
}
