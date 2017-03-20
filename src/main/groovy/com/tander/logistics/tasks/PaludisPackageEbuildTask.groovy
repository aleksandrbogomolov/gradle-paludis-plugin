package com.tander.logistics.tasks

import groovy.text.StreamingTemplateEngine
import groovy.text.XmlTemplateEngine
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import groovy.text.SimpleTemplateEngine
import groovy.text.Template

/**
 * Created by durov_an on 07.12.2016.
 */
class PaludisPackageEbuildTask extends DefaultTask {
    @Input String version
    @Input String baseName
    @InputFile File templateFile

    File ebuildFile

    PaludisPackageEbuildTask() {
        group = "distribution"
        description = "Generate ebuild file"
    }

    @TaskAction
    void run () {
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        Template template = engine.createTemplate(templateFile)
        ebuildFile = new File(project.buildDir, baseName + version + '.ebuild')
        ebuildFile.write(template.make().toString())
    }
}
