package com.tander.logistics.tasks

import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar

class PaludisPackageTarTask extends Tar {

    String packageName

    String dst

    List<String> src

    List<String> excludedFiles

    boolean isCollect

    PaludisPackageTarTask() {
        group = 'distribution'
    }

    @TaskAction
    void run() {
        archiveName = packageName
        compression = Compression.BZIP2
        extension = "tbz"
        destinationDir = new File(project.buildDir, "distributions")
        println("Inside run method.")
        into(dst) { from { src } }
    }
}
