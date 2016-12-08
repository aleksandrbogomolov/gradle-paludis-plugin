package com.tander.logistics.utils

import org.tmatesoft.svn.core.ISVNLogEntryHandler
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.SVNLogEntry

/**
 * Created by durov_an on 06.04.2016.
 */
class PaludisPackage {
    String packageName
    String setName
    String packageGroup
    String taskNumber
    String parentSet
    String EBUILDS_ROOT = "https://sources.corp.tander.ru/svn/real_out/pkg/repository"
    SVNUtils svnUtils

    def PaludisPackage(String packageGroup, String packageName, String svnUsername, char[] svnPassword) {
        this.packageGroup = packageGroup
        this.packageName = packageName
        svnUtils = new SVNUtils(svnUsername, svnPassword)
    }

    Build getBuildFromSet(String setNumber) {

        //скачать сет из SVN

        //регуляркой найти билд пакета
        return null
    }

    Build getBuildBySPPRTask(String spprTaskNumber) {

        Build build = new Build()

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
        svnUtils.doLog(getSVNURL(), 0, 0, isvnLogEntryHandler)
        return build
    }

    String getSVNURL() {
        return "$EBUILDS_ROOT/$packageGroup/$packageName"
    }

    Boolean isFilesChanged


}
