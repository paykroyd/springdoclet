package org.springdoclet

import com.sun.javadoc.ClassDoc
import com.sun.javadoc.AnnotationDesc

interface Collector {
  void processClass(ClassDoc classDoc, AnnotationDesc[] annotations)
  void writeOutput()
}