package com.dream11.grpc.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.reflections.Reflections;

@UtilityClass
public class AnnotationUtil {
  public List<Class<?>> getClassesWithAnnotation(
      String packageName, Class<? extends Annotation> annotation) {
    return new ArrayList<>(new Reflections(packageName).getTypesAnnotatedWith(annotation));
  }
}
