package com.tander.logistics.tasks

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
        description = "Create new ebuild file"
    }

    @TaskAction
    void run () {
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        XmlTemplateEngine xmlTemplateEngine = new XmlTemplateEngine()
        logger.lifecycle(templateFile.toString())
        Template template = engine.createTemplate(templateFile)
        Template xmlTemplate = xmlTemplateEngine.createTemplate(templateFile)
        logger.lifecycle('123123')
        ebuildFile = new File(project.buildDir, baseName + version + '.ebuild')
        logger.lifecycle('22222')
//        ebuildFile.write(template.make().toString())
        ebuildFile.write(xmlTemplate.make().toString())
    }
}
