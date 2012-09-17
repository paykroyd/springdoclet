package org.springdoclet.writers

/**
 * 
 */
class MarkdownBuilder {
    FileWriter fileWriter

    def MarkdownBuilder(fileWriter) {
        this.fileWriter = fileWriter
    }

    def b(str) {
        this.fileWriter.write("**" + str + "**")
        return this
    }

    def i(str) {
        this.fileWriter.write("__" + str + "__")
        return this
    }

    def h1(str) {
        this.fileWriter.write("# " + str + "\n")
        return this
    }

    def h2(str) {
        this.fileWriter.write("## " + str + "\n")
        return this
    }

    def h3(str) {
        this.fileWriter.write("### " + str + "\n")
        return this
    }

    def link(text, url) {
        this.fileWriter.write("[" + text + "](" + url + ")")
        return this
    }

    def code(str) {
        this.fileWriter.write("`" + str + "`")
        return this
    }

    def bullet() {
        this.fileWriter.write("* ")
        return this
    }

    def hr() {
        this.fileWriter.write("***\n")
        return this
    }

    def code_block(code) {
        this.fileWriter.write("```\n" + code + "\n```\n")
        return this
    }

    def code_block(code, language) {
        this.fileWriter.write("```" + language + "\n" + code + "\n```\n")
        return this
    }


    def br() {
        this.fileWriter.write("\n")
        return this
    }

    def write(str) {
        this.fileWriter.write(str)
        return this
    }

    def writeln(str) {
        this.fileWriter.write(str)
        this.br()
        return this
    }

}
