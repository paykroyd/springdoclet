package org.springdoclet.writers

import org.springdoclet.PathBuilder
import org.springdoclet.Configuration

/**
 *
 */
class MarkdownWriter {
    void writeOutput(List collectors, Configuration config) {
        File outputFile = getOutputFile(config.outputDirectory, config.outputFileName)
        PathBuilder paths = new PathBuilder(config.linkPath)
        def writer = new FileWriter(outputFile)
        def builder = new MarkdownBuilder(writer)

        builder.h1("Springpad API Documentation")

        for (collector in collectors)
            collector.writeMarkdown builder, paths

        writer.close()
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
