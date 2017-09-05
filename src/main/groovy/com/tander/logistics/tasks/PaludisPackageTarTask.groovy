package com.tander.logistics.tasks

import com.tander.logistics.PaludisPackageExtension
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar

class PaludisPackageTarTask extends Tar {

    PaludisPackageExtension ext

    PaludisPackageTarTask() {
        this.ext = project.extensions.tanderPaludis
        group = 'distribution'
        compression = Compression.BZIP2
        extension = "tbz"
        destinationDir = new File(project.buildDir, "distributions")
        onlyIf {
            def map = project.tasks.findByName("paludisPackageDistribution").property("paludisPackages") as HashMap
            archiveName = "$ext.packageName-$baseName-${project.version}.$extension"
            return map.get(name)
        }
    }
}
