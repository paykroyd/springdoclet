package org.springdoclet.writers

import groovy.xml.MarkupBuilder
import org.springdoclet.Configuration
import org.springdoclet.PathBuilder

class HtmlWriter {
    void writeOutput(List collectors, Configuration config) {
        File outputFile = getOutputFile(config.outputDirectory, config.outputFileName)
        PathBuilder paths = new PathBuilder(config.linkPath)
        def builder = new MarkupBuilder(new FileWriter(outputFile))

        builder.html {
            head {
                title 'Springpad API Endpoints'
                link(rel: 'stylesheet', type: 'text/css', href: 'https://springpad-www.s3.amazonaws.com/bootstrap/css/bootstrap.css')
                link(rel: 'stylesheet', type: 'text/css', href: 'https://springpad-www.s3.amazonaws.com/bootstrap/css/bootstrap-responsive.css')
                link(rel: 'stylesheet', type: 'text/css', href: 'https://springpad-www.s3.amazonaws.com/google-code-prettify/prettify.css')
                script(type: 'text/javascript', src: 'https://springpad-www.s3.amazonaws.com/google-code-prettify/prettify.js', '')
            }
            body(onload: 'prettyPrint()') {
                div(class: 'navbar navbar-fixed-top') {
                    div(class: 'navbar-inner') {
                        div(class: 'container') {
                            a(class: 'brand', 'Springpad API Endpoints')
                        }

                    }
                }

                div(class: 'container', style: 'padding-top:60px') {
                    h1('Springpad API Endpoint Documentation')
                    p('API endpoints for springpad.com')

                    for (collector in collectors) {
                        collector.writeOutput builder, paths
                    }
                }
            }
        }
    }

    private File getOutputFile(String outputDirectory, String outputFileName) {
        File path = new File(outputDirectory)
        if (!path.exists())
            path.mkdirs()

        def file = new File(path, outputFileName)
        file.delete()
        file.createNewFile()

        return file
    }
}
