package com.tander.logistics.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar

class PaludisPackageDistTask extends Tar {

    @Input
    String version

    PaludisPackageDistTask() {
        group = 'distribution'
        compression = Compression.BZIP2
        extension = "tbz"
        destinationDir = new File(project.buildDir, "distributions")
        from { "${project.buildDir}/dbrelease/${scriptType}.sql" }
    }
}
