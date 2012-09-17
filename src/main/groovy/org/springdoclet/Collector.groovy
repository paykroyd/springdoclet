package org.springdoclet

import com.sun.javadoc.ClassDoc
import com.sun.javadoc.AnnotationDesc
import groovy.xml.MarkupBuilder
import org.springdoclet.writers.MarkdownBuilder

interface Collector {
    void processClass(ClassDoc classDoc, AnnotationDesc[] annotations)
    void writeOutput(MarkupBuilder builder, PathBuilder paths)
    void writeMarkdown(MarkdownBuilder builder, PathBuilder paths)

}