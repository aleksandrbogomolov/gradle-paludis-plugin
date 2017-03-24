package com.tander.logistics.tasks

import com.tander.logistics.PaludisPackageExtension
import com.tander.logistics.svn.SvnUtils
import com.tander.logistics.core.PackageVersion
import com.tander.logistics.core.PaludisPackage
import com.tander.logistics.core.VersionType
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by durov_an on 01.04.2016.
 */
class PaludisPackageVersionTask extends DefaultTask {

    PackageVersion packageVersion
//    @Output String packageVersion

    PaludisPackage paludisPackage
    SvnUtils svnUtils
    PaludisPackageExtension ext

    PaludisPackageVersionTask() {
        group = "distribution"
        description = "get package version "
    }

    @TaskAction
    void run() {

        PaludisPackageExtension ext = project.extensions.paludis_package
        this.svnUtils = new SvnUtils(this.ext.user, this.ext.password.toCharArray())
        paludisPackage = new PaludisPackage(ext, svnUtils)
        packageVersion = paludisPackage.getBuildBySPPRTask(ext.spprTask)
//        logger.lifecycle(packageBuild.version)
        if (!packageVersion.version && ext.spprTask) {
            // если не нашли билд по задаче, то найдём последний билд
            packageVersion = paludisPackage.getBuildBySPPRTask(null)
            packageVersion.incVersion(VersionType.Minor)
        }
        if (!packageVersion.version) {
            packageVersion.version = '1.0.0'
        }

        logger.lifecycle("packageVersion - " + packageVersion.version)
        logger.lifecycle("baseName - " + project.name)

        ext.info.full = packageVersion.version
    }

//    @TaskAction
//    пока не используется
    def generateEbuild() {

        // определить каталог с ebuild'ами

        // вытянуть лог по каталогу

        // если собираем релиз,
        // то вытягиваем сет предыдущего релиза, находим в нём номер релиза нашего подпроекта
        //

//        packageVersion = paludisPackage.getBuildBySPPRTask(spprTaskNumber).version
//        if (!packageVersion || spprTaskNumber) {
//            packageVersion = paludisPackage.getBuildBySPPRTask(null).version
//        }

        // найти в логе последний коммит с номером задачи

        // если такой коммит есть, то берём номер из подходящего файла

        // вариант А, возвращаем новый номер

        // вариант Б, переименовываем старый файл, если есть

    }
}
