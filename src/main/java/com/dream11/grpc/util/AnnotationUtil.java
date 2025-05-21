package com.dream11.grpc.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.reflections.Reflections;

/**
 * Utility class for working with Java annotations. This class provides methods to scan and process
 * classes with specific annotations.
 */
@UtilityClass
public class AnnotationUtil {
  /**
   * Scans a specified package and returns all classes that are annotated with the given annotation.
   *
   * @param packageName The name of the package to scan for annotated classes
   * @param annotation The annotation class to look for
   * @return A list of classes that are annotated with the specified annotation
   */
  public List<Class<?>> getClassesWithAnnotation(
      String packageName, Class<? extends Annotation> annotation) {
    return new ArrayList<>(new Reflections(packageName).getTypesAnnotatedWith(annotation));
  }
}
