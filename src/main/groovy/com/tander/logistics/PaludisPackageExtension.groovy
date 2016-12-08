package com.tander.logistics


/**
 * Created by durov_an on 10.02.2016.
 *
 * Настройки плагина
 */
class PaludisPackageExtension {
    String previousBranch = ''
    String packageName = ''
    String packageGroup = ''
    String spprTask = ''
    String user = ''
    char[] password
    String ebuildTemplate = ''
    private VersionInfo info

    VersionInfo getInfo() {
        info
    }

    PaludisPackageExtension() {
        info = new VersionInfo()
    }
}
