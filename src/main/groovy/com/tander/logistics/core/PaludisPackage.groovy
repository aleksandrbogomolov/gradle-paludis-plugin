package com.tander.logistics.core

import com.tander.logistics.PaludisPackageExtension
import com.tander.logistics.svn.SvnBranch
import com.tander.logistics.svn.SvnUtils
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.tmatesoft.svn.core.*
import org.tmatesoft.svn.core.wc.*

/**
 * Created by durov_an on 06.04.2016.
 */
class PaludisPackage {

    protected Logger logger
    PaludisPackageExtension ext
    String setName
    String setVersion
    String packageName
    String packageGroup
    String taskNumber
    String parentSet
//    String EBUILDS_ROOT = "https://sources.corp.tander.ru/svn/real_out/pkg/repository"
//    String TBZS_ROOT = "https://sources.corp.tander.ru/svn/real_out/pkg/distfiles"
    String PALUDIS_ROOT = "https://sources.corp.tander.ru/svn/real_out/pkg"
    SvnUtils svnUtils
    File releaseDir
    SvnBranch currBranch
    SvnBranch prevBranch

    def PaludisPackage(Project project, SvnUtils svnUtils) {
        this.ext = project.extensions.paludis_package
        this.svnUtils = svnUtils

        logger = Logging.getLogger(this.class)

        releaseDir = new File(project.buildDir.getPath(), "paludis")
        releaseDir.deleteDir()

        packageGroup = ext.packageGroup
        packageName = ext.packageName
        setName = ext.setName

        currBranch = new SvnBranch(svnUtils, null, null, null)
        prevBranch = new SvnBranch(svnUtils, null, null, null)
        if (ext.currUrl) {
            currBranch.url = ext.currUrl
        } else {
            currBranch.url = currBranch.getUrlFromFolder(project.projectDir.toString())
        }

        if (ext.currRevision) {
            currBranch.revision = SVNRevision.create(ext.currRevision as long)
        } else {
            currBranch.revision = SVNRevision.create(currBranch.getLastRevision() as long)
//            currBranch.revision = SVNRevision.HEAD
        }

        if (ext.releaseVersion) {
            currBranch.version = ext.releaseVersion
        } else {
            ext.releaseVersion = currBranch.getReleaseNumberFromUrl()
            currBranch.version = ext.releaseVersion
        }

        if (ext.prevUrl) {
            prevBranch.url = ext.prevUrl
            prevBranch.revision = SVNRevision.create(prevBranch.getLastRevision() as long)
        } else {
            prevBranch.url = currBranch.url
            prevBranch.revision = SVNRevision.create(prevBranch.getFirstRevision() as long)
        }

        prevBranch.version = prevBranch.getReleaseNumberFromUrl()

        if (ext.prevRevision) {
            prevBranch.revision = SVNRevision.create(ext.prevRevision as long)
        }

        svnUtils.testConnection(currBranch.url)
    }

    PackageVersion getBuildFromSet(String setNumber) {

        //скачать сет из SVN

        //регуляркой найти билд пакета
        return null
    }

    PackageVersion getBuildBySPPRTask(String spprTaskNumber) {

        PackageVersion build = new PackageVersion()

        ISVNLogEntryHandler isvnLogEntryHandler = new ISVNLogEntryHandler() {
            @Override
            void handleLogEntry(SVNLogEntry logEntry) throws SVNException {

                // определим с помощью регулярки в какую задачу был коммит
                def matcher = (logEntry.getMessage() =~ /#(SP\d+)/)
                def revisionSPPRTask = matcher ? matcher.group(1) : null
                if (spprTaskNumber == null || spprTaskNumber.equals(revisionSPPRTask)) {
                    // если это наша задача, то поищем версию закоммиченного пакета
                    // если номер СППР не передан, то вернём последнюю версию
                    logEntry.getChangedPaths().each {
                        matcher = (it.key =~ /$packageName-(\d+\.\d+\.\d+(?:\.\d+)?)/)
                        if (matcher) {
                            build.version = matcher.group(1)
                            matcher = (build.version =~ /(\d+)\.(\d+)\.(\d+)(?:\.(\d+))?/)
                            if (matcher) {
                                build.versionMajor = matcher.group(1) ?: null
                                build.versionMinor = matcher.group(2) ?: null
                                build.versionRelease = matcher.group(3) ?: null
                                build.versionBuild = matcher.group(4) ?: null
                            }
                        }
                    }
                }
            }
        }
        // запуск обработки лога SVN
        svnUtils.doLog(getPackageEbuildDirUrl(), SVNRevision.create(0), SVNRevision.create(0), 0, isvnLogEntryHandler)
        return build
    }

    def getPackageVersions(String group, String name) {
        ArrayList packageVersions = []
        ISVNDirEntryHandler entryHandler = new ISVNDirEntryHandler() {
            @Override
            void handleDirEntry(SVNDirEntry svnDirEntry) throws SVNException {

                def m = svnDirEntry.getRelativePath() =~ /${name}-(\.+).ebuild/
                def version = ""
                if (m.matches()) {
                    version = m.group(1)
                    packageVersions.add(version)
                } else {
                    version = ""
                }
            }
        }
        svnUtils.doList(
                "$PALUDIS_ROOT/repository/$group/$name",
                entryHandler
        )
    }

    String getPackageEbuildDirUrl() {
        return "$PALUDIS_ROOT/repository/$packageGroup/$packageName"
    }

    String getPackageTbzDirUrl() {
        return "$PALUDIS_ROOT/distfiles"
    }

    String getSetEbuildDirUrl() {
        return "$PALUDIS_ROOT/set/$setName"
    }

    def initValues() {
        // определить номер релиза
        // если ветка в каталоге releases то номер = наименованию ветки
        // если
    }

    boolean isFilesChanged() {

        // 1) сравнить старую и новую ветки. Если есть изменения нужно собирать
        // 2) сравнить
        // compareSVNBranches;

        boolean isFileChanged = false

        ISVNDiffStatusHandler diffStatusHandler = new ISVNDiffStatusHandler() {
            ScmFile scmFile

            @Override
            void handleDiffStatus(SVNDiffStatus svnDiffStatus) throws SVNException {
                if (svnDiffStatus.getKind() == SVNNodeKind.FILE) {
                    scmFile = new ScmFile(svnDiffStatus.getPath())
                    scmFile.url = svnDiffStatus.getURL().toString()
                    if (svnDiffStatus.getModificationType() in [SVNStatusType.STATUS_MODIFIED,
                                                                SVNStatusType.STATUS_DELETED,
                                                                SVNStatusType.STATUS_ADDED]) {
                        isFileChanged = true
                    }
                }
                logger.info(svnDiffStatus.getModificationType().toString() + ' ' + svnDiffStatus.getFile().toString())
            }
        }
        logger.lifecycle("--------------- diff start ---------------")
        svnUtils.doDiffStatus(prevBranch.url,
                prevBranch.revision,
                currBranch.url,
                currBranch.revision,
                diffStatusHandler)
        logger.lifecycle("--------------- diff finish ---------------")

        return isFileChanged
    }

    String generatePackageVersion() {
        // вытащить историю коммитов в ebuild
        // если есть хоть один коммит с указанным номером запроса, то используем номер в этом ebuild
        // если таких коммитов нет, то если файлы изменились - увеличиваем номер
        // если файлы не изменились, то ищем последний коммит в продакшн сборку с номером релиза меньше или равно указанному
        // если это официальная сборка, то номер равен номеру сборки
    }

    String getPackageVersion() {
        initValues()
        if (isFilesChanged()) {
            // если произошли изменения, то новый номер версии = номеру релиза
            ext.info.full = currBranch.version
            return currBranch.version
//            если собираем не релиз, то версия = номеру запроса в СППР? нужен генератор номеров
//            generatePackageVersion();
        } else {
            // если изменений нет, то нужно взять номер из ebuild файла с предыдущим сетом
            // получить номер предыдущего сета
            ArrayList setVersions = getPackageVersions("set", setName)
            def curSetVersionIndex = setVersions.findIndexOf {
                it == setVersion
            }
            // а предыдущего сета может не быть, тогда создаём? неет.
            if (curSetVersionIndex == -1) {
                return currBranch.version
            }
            String prevSetVersion = setVersions[curSetVersionIndex - 1]
            // выгрузить его
            exportPackage("set", setName, prevSetVersion)
            // сделать парсинг файла, по имени пакета
            return getVersionFromEbuild("set", setName, prevSetVersion)
//            ext.info.full = generatePackageVersion()
        }
    }

    String getVersionFromEbuild(String group, String name, String version) {
        File setEbuildFile = new File(releaseDir.path + "/repository/$group/$name/$name-${version}.ebuild")
        def pattern = /\=${this.packageGroup}\/${this.packageName}-(\.+)/
        def matcher = setEbuildFile.getText() =~ pattern
        def m = message =~ /(?s)(#SP\d+).*/
        def prevVersion = matcher ? matcher.group(1) : null
        return prevVersion
    }

    def exportPackage(String group, String name, String version) {
        logger.lifecycle("--------------- export start ---------------")
        ISVNEventHandler dispatcher = new ISVNEventHandler() {
            @Override
            void handleEvent(SVNEvent svnEvent, double v) throws SVNException {
                if (svnEvent.getAction() == SVNEventAction.UPDATE_COMPLETED) {
                    logger.lifecycle(" export file " + svnEvent.getFile().toString())
                }
            }

            @Override
            void checkCancelled() throws SVNCancelException {
            }
        }

        svnUtils.doExport("$PALUDIS_ROOT/repository/$group/$name/$name-${version}.ebuild",
                releaseDir.path + "/repository/$group/$name/$name-${version}.ebuild",
                SVNRevision.HEAD,
                dispatcher)

        svnUtils.doExport("$PALUDIS_ROOT/distfiles/$name-${version}.tbz",
                releaseDir.path + "/$group/$name/$name-${version}.ebuild",
                SVNRevision.HEAD,
                dispatcher)

        logger.lifecycle("--------------- export finish ---------------")
    }
}
