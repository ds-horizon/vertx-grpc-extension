package com.dream11.grpc.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.dream11.grpc.annotation.GrpcService;
import java.lang.annotation.Annotation;
import java.util.List;
import org.junit.jupiter.api.Test;

class AnnotationUtilTest {

  @Test
  void testGetClassesWithAnnotation() {
    // arrange
    String packageName = "com.dream11.grpc";
    Class<? extends Annotation> annotation = GrpcService.class;

    // act
    List<Class<?>> annotatesClasses =
        AnnotationUtil.getClassesWithAnnotation(packageName, annotation);

    // assert
    assertThat(annotatesClasses).hasSize(1);
  }
}
