package com.tander.logistics

import com.tander.logistics.tasks.PaludisPackageEbuildTask
import com.tander.logistics.tasks.PaludisPackageVersionTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by durov_an on 07.12.2016.
 *
 * Плагин для работы с пакетами палудиса
 */
class PaludisPackagePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create('tanderPaludis', PaludisPackageExtension)
        project.tasks.create('paludisPackageVersion', PaludisPackageVersionTask)
        project.tasks.create('paludisPackageEbuild', PaludisPackageEbuildTask)
    }
}
