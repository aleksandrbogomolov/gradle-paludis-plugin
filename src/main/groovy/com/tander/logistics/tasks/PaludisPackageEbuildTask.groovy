package com.tander.logistics.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Created by durov_an on 07.12.2016.
 */
class PaludisPackageEbuildTask extends DefaultTask {

    @Input
    String version
    @Input
    String baseName

    PaludisPackageEbuildTask() {
        group = "distribution"
        description = "Generate ebuild file"
    }

    @TaskAction
    void run() {
        def tree = project.fileTree(new File("template"))
        tree.getDir().listFiles().each { file ->
            new File(project.buildDir, "$baseName-${file.name}-${version}.ebuild").text = file.text
        }
    }
}
