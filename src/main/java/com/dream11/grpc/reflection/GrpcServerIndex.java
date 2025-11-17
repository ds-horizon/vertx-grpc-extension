package com.dream11.grpc.reflection;

import com.google.protobuf.Descriptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServiceDescriptor;
import io.grpc.protobuf.ProtoFileDescriptorSupplier;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class GrpcServerIndex {

  private final Set<String> names;
  private final Map<String, Descriptors.FileDescriptor> descriptorsByName;
  private final Map<String, Descriptors.FileDescriptor> descriptorsBySymbol;
  private final Map<String, Map<Integer, Descriptors.FileDescriptor>>
      descriptorsByExtensionAndNumber;

  public GrpcServerIndex(List<ServerServiceDefinition> definitions) {
    Queue<Descriptors.FileDescriptor> fileDescriptorsToProcess = new ArrayDeque<>();
    Set<String> files = new HashSet<>();
    Set<String> serviceNames = new HashSet<>();
    Map<String, Descriptors.FileDescriptor> fileDescriptorsByName = new LinkedHashMap<>();
    Map<String, Descriptors.FileDescriptor> fileDescriptorsBySymbol = new LinkedHashMap<>();
    Map<String, Map<Integer, Descriptors.FileDescriptor>> fileDescriptorsByExtensionAndNumber =
        new LinkedHashMap<>();

    // Collect the services
    for (ServerServiceDefinition definition : definitions) {
      ServiceDescriptor serviceDescriptor = definition.getServiceDescriptor();
      if (serviceDescriptor.getSchemaDescriptor() instanceof ProtoFileDescriptorSupplier) {
        ProtoFileDescriptorSupplier supplier =
            (ProtoFileDescriptorSupplier) serviceDescriptor.getSchemaDescriptor();
        Descriptors.FileDescriptor fd = supplier.getFileDescriptor();
        String serviceName = serviceDescriptor.getName();
        if (serviceNames.contains(serviceName)) {
          throw new IllegalStateException("Duplicated gRPC service: " + serviceName);
        }
        serviceNames.add(serviceName);

        if (!files.contains(fd.getName())) {
          files.add(fd.getName());
          fileDescriptorsToProcess.add(fd);
        }
      }
    }

    // Traverse the set of service and add dependencies
    while (!fileDescriptorsToProcess.isEmpty()) {
      Descriptors.FileDescriptor fd = fileDescriptorsToProcess.remove();
      this.processFileDescriptor(
          fd, fileDescriptorsByName, fileDescriptorsBySymbol, fileDescriptorsByExtensionAndNumber);
      for (Descriptors.FileDescriptor dep : fd.getDependencies()) {
        if (!files.contains(dep.getName())) {
          files.add(dep.getName());
          fileDescriptorsToProcess.add(dep);
        }
      }
    }

    this.descriptorsByName = Collections.unmodifiableMap(fileDescriptorsByName);
    this.descriptorsByExtensionAndNumber =
        Collections.unmodifiableMap(fileDescriptorsByExtensionAndNumber);
    this.descriptorsBySymbol = Collections.unmodifiableMap(fileDescriptorsBySymbol);
    this.names = Collections.unmodifiableSet(serviceNames);
  }

  public Set<String> getServiceNames() {
    return this.names;
  }

  public Descriptors.FileDescriptor getFileDescriptorByName(String name) {
    return this.descriptorsByName.get(name);
  }

  public Descriptors.FileDescriptor getFileDescriptorBySymbol(String symbol) {
    return this.descriptorsBySymbol.get(symbol);
  }

  public Descriptors.FileDescriptor getFileDescriptorByExtensionAndNumber(String type, int number) {
    return this.descriptorsByExtensionAndNumber.getOrDefault(type, Map.of()).get(number);
  }

  public Set<Integer> getExtensionNumbersOfType(String type) {
    return this.descriptorsByExtensionAndNumber.getOrDefault(type, Map.of()).keySet();
  }

  private void processFileDescriptor(
      Descriptors.FileDescriptor fd,
      Map<String, Descriptors.FileDescriptor> descriptorsByName,
      Map<String, Descriptors.FileDescriptor> descriptorsBySymbol,
      Map<String, Map<Integer, Descriptors.FileDescriptor>> descriptorsByExtensionAndNumber) {
    String name = fd.getName();
    if (descriptorsByName.containsKey(name)) {
      throw new IllegalStateException("File name already used: " + name);
    }
    descriptorsByName.put(name, fd);
    fd.getServices().forEach(service -> this.processService(service, fd, descriptorsBySymbol));
    fd.getMessageTypes()
        .forEach(
            type ->
                this.processType(type, fd, descriptorsBySymbol, descriptorsByExtensionAndNumber));
    fd.getExtensions()
        .forEach(
            extension -> this.processExtension(extension, fd, descriptorsByExtensionAndNumber));
  }

  private void processService(
      Descriptors.ServiceDescriptor service,
      Descriptors.FileDescriptor fd,
      Map<String, Descriptors.FileDescriptor> descriptorsBySymbol) {
    String fullyQualifiedServiceName = service.getFullName();
    if (descriptorsBySymbol.containsKey(fullyQualifiedServiceName)) {
      throw new IllegalStateException("Service already defined: " + fullyQualifiedServiceName);
    }
    descriptorsBySymbol.put(fullyQualifiedServiceName, fd);
    for (Descriptors.MethodDescriptor method : service.getMethods()) {
      String fullyQualifiedMethodName = method.getFullName();
      if (descriptorsBySymbol.containsKey(fullyQualifiedMethodName)) {
        throw new IllegalStateException(
            "Method already defined: "
                + fullyQualifiedMethodName
                + " in "
                + fullyQualifiedServiceName);
      }
      descriptorsBySymbol.put(fullyQualifiedMethodName, fd);
    }
  }

  private void processType(
      Descriptors.Descriptor type,
      Descriptors.FileDescriptor fd,
      Map<String, Descriptors.FileDescriptor> descriptorsBySymbol,
      Map<String, Map<Integer, Descriptors.FileDescriptor>> descriptorsByExtensionAndNumber) {
    String fullyQualifiedTypeName = type.getFullName();
    if (descriptorsBySymbol.containsKey(fullyQualifiedTypeName)) {
      throw new IllegalStateException("Type already defined: " + fullyQualifiedTypeName);
    }
    descriptorsBySymbol.put(fullyQualifiedTypeName, fd);
    type.getExtensions()
        .forEach(
            extension -> this.processExtension(extension, fd, descriptorsByExtensionAndNumber));
    type.getNestedTypes()
        .forEach(
            nestedType ->
                this.processType(
                    nestedType, fd, descriptorsBySymbol, descriptorsByExtensionAndNumber));
  }

  private void processExtension(
      Descriptors.FieldDescriptor extension,
      Descriptors.FileDescriptor fd,
      Map<String, Map<Integer, Descriptors.FileDescriptor>> descriptorsByExtensionAndNumber) {
    String extensionName = extension.getContainingType().getFullName();
    int extensionNumber = extension.getNumber();

    descriptorsByExtensionAndNumber.computeIfAbsent(extensionName, s -> new HashMap<>());

    if (descriptorsByExtensionAndNumber.get(extensionName).containsKey(extensionNumber)) {
      throw new IllegalStateException(
          "Extension name "
              + extensionName
              + " and number "
              + extensionNumber
              + " are already defined");
    }
    descriptorsByExtensionAndNumber.get(extensionName).put(extensionNumber, fd);
  }
}
