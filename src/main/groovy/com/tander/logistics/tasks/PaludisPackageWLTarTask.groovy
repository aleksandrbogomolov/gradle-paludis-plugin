package com.tander.logistics.tasks

import com.tander.logistics.PaludisPackageExtension
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar

/**
 * Created by bogomolov_av on 22.03.2018
 */
class PaludisPackageWLTarTask extends Tar {

    PaludisPackageExtension ext

    boolean forceDistribution

    PaludisPackageWLTarTask() {
        this.ext = project.extensions.tanderPaludis
        group = 'distribution'
        compression = Compression.BZIP2
        extension = "tbz"
        destinationDir = new File(project.buildDir, "distributions")
        onlyIf {
            def map = project.tasks.findByName("paludisPackageDistribution").property("paludisPackages") as HashMap
            archiveName = "$ext.packageNameWl-$baseName-${project.tasks.findByName("paludisPackageDistribution").property("packageVersion").version}.$extension"
            return map.get(baseName) || forceDistribution
        }
    }
}
