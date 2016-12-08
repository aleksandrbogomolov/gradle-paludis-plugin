package com.tander.logistics.tasks

import com.tander.logistics.PaludisPackageExtension
import com.tander.logistics.utils.Build
import com.tander.logistics.utils.PaludisPackage
import com.tander.logistics.utils.VersionType
import groovy.json.JsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Created by durov_an on 01.04.2016.
 */
class PaludisPackageVersionTask extends DefaultTask {

    Build packageBuild
    String packageVersion
//    @Output String packageVersion

    PaludisPackage paludisPackage

    PaludisPackageVersionTask() {
        group = "distribution"
        description = "Writes version information on the standard output."
    }

    @TaskAction
    void run() {
        PaludisPackageExtension packageExtension = project.extensions.paludis_package

//        logger.lifecycle(new JsonBuilder(packageExtension).toString())
//        logger.lifecycle(packageExtension.packageGroup)
//        logger.lifecycle(packageExtension.packageName)
//        logger.lifecycle(packageExtension.user)
//        logger.lifecycle(packageExtension.password.toString())
//        logger.lifecycle(packageExtension.spprTask)

        paludisPackage = new PaludisPackage(packageExtension.packageGroup,
                packageExtension.packageName,
                packageExtension.user,
                packageExtension.password)
        packageBuild = paludisPackage.getBuildBySPPRTask(packageExtension.spprTask)
//        logger.lifecycle(packageBuild.version)
        if (!packageBuild.version && packageExtension.spprTask) {
            // если не нашли билд по задаче, то найдём последний билд
            packageBuild = paludisPackage.getBuildBySPPRTask(null)
            packageBuild.incVersion(VersionType.Minor)
        }
        if (!packageBuild.version) {
            packageBuild.version = '1.0.0'
        }

        logger.lifecycle("packageVersion - " + packageBuild.version)
        logger.lifecycle("baseName - " + project.name)

        packageExtension.info.full = packageBuild.version
    }

//    @TaskAction
//    пока не используется
    def generateEbuild() {

        // определить каталог с ebuild'ами

        // вытянуть лог по каталогу

        // если собираем релиз,
        // то вытягиваем сет предыдущего релиза, находим в нём номер релиза нашего подпроекта
        //

        packageVersion = paludisPackage.getBuildBySPPRTask(spprTaskNumber).version
        if (!packageVersion || spprTaskNumber) {
            packageVersion = paludisPackage.getBuildBySPPRTask(null).version
        }

        // найти в логе последний коммит с номером задачи

        // если такой коммит есть, то берём номер из подходящего файла

        // вариант А, возвращаем новый номер

        // вариант Б, переименовываем старый файл, если есть

    }
}
