package com.tander.logistics

import org.gradle.api.Project

/**
 * Created by durov_an on 10.02.2016.
 *
 * Настройки плагина
 */
class PaludisPackageExtension {

    Project project
    String user = ''
    String password = ''

    String releaseVersion
    String currUrl
    String prevUrl
    String currRevision
    String prevRevision
    String packagePath

    String packageName = ''
    String setName = ''
    private VersionInfo info

    VersionInfo getInfo() {
        info
    }

    PaludisPackageExtension(Project project) {
        info = new VersionInfo()
        this.project = project
    }

    void init(Project project) {
        this.project = project

        if (project.hasProperty("domainUser")) {
            user = project.property("domainUser")
        }

        if (project.hasProperty("domainPassword")) {
            password = project.property("domainPassword")
        }

        if (!password) {
            password = ''
        }
    }
}
