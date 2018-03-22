package com.tander.logistics

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
        PaludisPackageExtension ext = project.extensions.create('tanderPaludis', PaludisPackageExtension, project)

        project.afterEvaluate {
            ext.init(project)
        }
    }
}
