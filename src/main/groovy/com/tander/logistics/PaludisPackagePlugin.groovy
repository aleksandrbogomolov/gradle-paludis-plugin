package com.tander.logistics

import com.tander.logistics.tasks.PaludisPackageDistributionTask
import com.tander.logistics.tasks.PaludisPackageEbuildTask
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
        project.tasks.create('paludisPackageDistribution', PaludisPackageDistributionTask)
        project.tasks.create('paludisPackageEbuild', PaludisPackageEbuildTask)
//        project.afterEvaluate {
//            project.tasks.findByName('ttar').dependsOn(project.tasks.findByName('libs'))
//            project.tasks.findByName('ttar').dependsOn(project.tasks.findByName('img'))
////            project.tasks.findByName('ttar').dependsOn(project.tasks.findByName('bin'))
//        }
    }
}
