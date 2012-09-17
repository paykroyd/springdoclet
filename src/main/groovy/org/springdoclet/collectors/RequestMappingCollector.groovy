package org.springdoclet.collectors

import com.sun.javadoc.AnnotationDesc
import com.sun.javadoc.ClassDoc
import groovy.xml.MarkupBuilder
import org.springdoclet.Collector
import org.springdoclet.Annotations
import org.springdoclet.PathBuilder
import org.springdoclet.TextUtils
import org.springdoclet.writers.MarkdownBuilder

@SuppressWarnings("GroovyVariableNotAssigned")
class RequestMappingCollector implements Collector {
    private static String MAPPING_TYPE = 'org.springframework.web.bind.annotation.RequestMapping'
    private static String METHOD_TYPE = 'org.springframework.web.bind.annotation.RequestMethod.'

    private mappings = []

    void processClass(ClassDoc classDoc, AnnotationDesc[] annotations) {
        def annotation = getMappingAnnotation(annotations)
        if (annotation) {
            def rootPath, defaultHttpMethods
            (rootPath, defaultHttpMethods) = getMappingElements(annotation)
            processMethods classDoc, rootPath ?: "", defaultHttpMethods ?: ['GET']
        } else {
            processMethods classDoc, "", ['GET']
        }
    }

    private void processMethods(classDoc, rootPath, defaultHttpMethods) {
        def methods = classDoc.methods(true)
        for (method in methods) {
            for (annotation in method.annotations()) {
                def annotationType = Annotations.getTypeName(annotation)
                if (annotationType?.startsWith(MAPPING_TYPE)) {
                    processMethod classDoc, method, rootPath, defaultHttpMethods, annotation
                }
            }
        }
    }

    private def processMethod(classDoc, methodDoc, rootPath, defaultHttpMethods, annotation) {
        def (path, httpMethods) = getMappingElements(annotation)
        for (httpMethod in (httpMethods ?: defaultHttpMethods)) {
            addMapping classDoc, methodDoc, path, httpMethod
        }
    }

    private def getMappingAnnotation(annotations) {
        for (annotation in annotations) {
            def annotationType = Annotations.getTypeName(annotation)
            if (annotationType?.startsWith(MAPPING_TYPE)) {
                return annotation
            }
        }
        return null
    }

    private def getMappingElements(annotation) {
        def elements = annotation.elementValues()
        def path = getElement(elements, "value") ?: ""
        def httpMethods = getElement(elements, "method")?.value()
        return [path, httpMethods]
    }

    private def getElement(elements, key) {
        for (element in elements) {
            if (element.element().name() == key) {
                return element.value()
            }
        }
        return null
    }

    private void addMapping(classDoc, methodDoc, path, httpMethod) {
        def httpMethodName = httpMethod.toString() - METHOD_TYPE
        def apiPrefix = null
        if (classDoc.tags('api.prefix').length > 0)
            apiPrefix = classDoc.tags('api.prefix')[0].text()
        if (apiPrefix != null) {
            mappings << [path: path,
                    httpMethodName: httpMethodName,
                    className: classDoc.qualifiedTypeName(),
                    apiPrefix: apiPrefix,
                    jparams: methodDoc.tags('param'),
                    params: methodDoc.tags('api.param'),
                    returns: methodDoc.tags('api.returns'),
                    postData: methodDoc.tags('api.post-data'),
                    errors: methodDoc.tags('api.error'),
                    headers: methodDoc.tags('api.header'),
                    text: TextUtils.getFirstSentence(methodDoc.commentText())]
        }
    }

    void writeMarkdown(MarkdownBuilder builder, PathBuilder paths) {
        def sortedMappings = mappings.sort { it.className }

        for (mapping in sortedMappings) {
            def path = mapping.path.toString().replaceAll('"', '')
            def text = mapping.text.toString()
            def fullPath = mapping.httpMethodName + " " + (mapping.apiPrefix != null ? mapping.apiPrefix + path : path)

            builder.h2(fullPath).br()
            if (text)
                builder.writeln(text)

            if (mapping.headers) {
                builder.h3("Headers")
                for (param in mapping.headers) {
                    def paramText = param.text().toString()
                    def paramName = paramText.substring(0, paramText.contains(' ') ? paramText.indexOf(' ') : paramText.length())
                    def paramDesc = null
                    if (paramText.contains(' '))
                        paramDesc = paramText.substring(paramText.indexOf(' ') + 1)

                    builder.bullet().i(paramName).write(" ").writeln(paramDesc)
                }
                builder.br()
            }

//            if (mapping.jparams) {
//                builder.h3("Query Params")
//                for (param in mapping.jparams) {
//                    def paramText = param.text().toString()
//                    def paramName = paramText.substring(0, paramText.contains(' ') ? paramText.indexOf(' ') : paramText.length())
//                    def paramDesc = null
//                    if (paramText.contains(' '))
//                        paramDesc = paramText.substring(paramText.indexOf(' ') + 1)
//                    else
//                        paramDesc = ""
//                    builder.bullet().i(paramName).write(" ").writeln(paramDesc)
//                }
//                builder.br()
//            }

            if (mapping.params.length > 0) {
                builder.h3("Query Params")
                for (param in mapping.params) {
                    def paramText = param.text().toString()
                    def paramName = paramText.substring(0, paramText.contains(' ') ? paramText.indexOf(' ') : paramText.length())
                    def paramDesc = ""
                    if (paramText.contains(' '))
                        paramDesc = paramText.substring(paramText.indexOf(' ') + 1)
                    builder.bullet().i(paramName).write(" ").writeln(paramDesc)
                }
                builder.br()
            }

            if (mapping.postData.length > 0) {
                builder.h3("Post Data")
                for (item in mapping.postData) {
                    builder.code_block(item.text())
                }
            }

            if (mapping.returns.length > 0) {
                builder.h3("Returns")
                for (item in mapping.returns) {
                    builder.code_block(item.text())
                }
            }

            if (mapping.errors.length > 0) {
                builder.h3("Errors")

                for (param in mapping.errors) {
                    def paramText = param.text().toString()
                    def paramName = paramText.substring(0, paramText.contains(' ') ? paramText.indexOf(' ') : paramText.length())
                    def paramDesc = ""
                    if (paramText.contains(' '))
                        paramDesc = paramText.substring(paramText.indexOf(' ') + 1)
                    builder.bullet().i(paramName).write(" ").writeln(paramDesc)
                }
                builder.br()
            }

            builder.hr()
        }
    }

    void writeOutput(MarkupBuilder builder, PathBuilder paths) {
        builder.div(id: 'request-mappings') {
            def sortedMappings = mappings.sort { it.className }

            for (mapping in sortedMappings) {
                def path = mapping.path.toString().replaceAll('"', '')
                def className = paths.buildFilePath(mapping.className)
                def text = mapping.text.toString()
                def fullPath = mapping.httpMethodName + " " + (mapping.apiPrefix != null ? mapping.apiPrefix + path : path)

                a('name': fullPath, '')

                div(class: 'well') {
                    a('href': '#' + fullPath) {
                        h3(fullPath)
                    }

                    hr(style: 'margin:3px')
                    p { mkp.yieldUnescaped(text ?: ' ') }

                    if (mapping.headers.length > 0) {
                        span(class: 'label label-success', style: 'margin-bottom:6px', 'Headers')

                        table(class: 'table table-bordered table-striped table-condensed') {
                            for (param in mapping.headers) {
                                def paramText = param.text().toString()
                                def paramName = paramText.substring(0, paramText.contains(' ') ? paramText.indexOf(' ') : paramText.length())
                                def paramDesc = null
                                if (paramText.contains(' '))
                                    paramDesc = paramText.substring(paramText.indexOf(' ') + 1)
                                tr {
                                    td(paramName)
                                    td(paramDesc)
                                }
                            }
                        }
                    }

                    if (mapping.jparams.length > 0) {
                        span(class: 'label label-success', style: 'margin-bottom:6px', 'Path Parameters')

                        table(class: 'table table-bordered table-striped table-condensed') {
                            for (param in mapping.jparams) {
                                def paramText = param.text().toString()
                                def paramName = paramText.substring(0, paramText.contains(' ') ? paramText.indexOf(' ') : paramText.length())
                                def paramDesc = null
                                if (paramText.contains(' '))
                                    paramDesc = paramText.substring(paramText.indexOf(' ') + 1)
                                tr {
                                    td(paramName)
                                    td(paramDesc)
                                }
                            }
                        }
                    }

                    if (mapping.params.length > 0) {
                        span(class: 'label label-success', style: 'margin-bottom:6px', 'Parameters')

                        table(class: 'table table-bordered table-striped table-condensed') {
                            for (param in mapping.params) {
                                def paramText = param.text().toString()
                                def paramName = paramText.substring(0, paramText.contains(' ') ? paramText.indexOf(' ') : paramText.length())
                                def paramDesc = null
                                if (paramText.contains(' '))
                                    paramDesc = paramText.substring(paramText.indexOf(' ') + 1)
                                tr {
                                    td(paramName)
                                    td(paramDesc)
                                }
                            }
                        }
                    }

                    if (mapping.postData.length > 0) {
                        span(class: 'label label-success', style: 'margin-bottom:6px', 'Post')

                        div {
                            for (item in mapping.postData) {
                                pre(class: 'prettyprint lang-js', item.text())
                            }
                        }
                    }

                    if (mapping.returns.length > 0) {
                        span(class: 'label label-success', style: 'margin-bottom:6px', 'Returns')

                        div {
                            for (returnDescription in mapping.returns) {
                                pre(class: 'prettyprint lang-js', returnDescription.text())
                            }
                        }
                    }

                    if (mapping.errors.length > 0) {
                        span(class: 'label label-success', style: 'margin-bottom:6px', 'Errors')

                        table(class: 'table table-bordered table-striped table-condensed') {
                            for (param in mapping.errors) {
                                def paramText = param.text().toString()
                                def paramName = paramText.substring(0, paramText.contains(' ') ? paramText.indexOf(' ') : paramText.length())
                                def paramDesc = null
                                if (paramText.contains(' '))
                                    paramDesc = paramText.substring(paramText.indexOf(' ') + 1)
                                tr {
                                    td(paramName)
                                    td(paramDesc)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
