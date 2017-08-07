package com.tander.logistics

import org.gradle.api.Project


/**
 * Created by durov_an on 10.02.2016.
 *
 * Настройки плагина
 */
class PaludisPackageExtension {

    Project project
    Boolean isTest = false
    String scmType
    String user = ''
    String password = ''

    String taskNumber
    String releaseVersion
    String currUrl
    String prevUrl
    String currRevision
    String prevRevision

    String isCheckReleaseNumberNeeded
    String isUpdateReleaseNumberNeeded

    String buildTaskNumber

    String packageName = ''
    String packageGroup = ''
    String setName = ''
    String spprTask = ''
    String ebuildTemplate = ''
    private VersionInfo info

    VersionInfo getInfo() {
        info
    }

    PaludisPackageExtension() {
        info = new VersionInfo()
    }
}
