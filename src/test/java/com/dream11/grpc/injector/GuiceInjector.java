package com.dream11.grpc.injector;

import com.dream11.grpc.ClassInjector;
import com.google.inject.Injector;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GuiceInjector implements ClassInjector {
  final Injector injector;

  @Override
  public <T> T getInstance(Class<T> clazz) {
    return this.injector.getInstance(clazz);
  }
}
