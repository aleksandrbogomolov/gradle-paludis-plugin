package com.tander.logistics.utils

import com.tander.logistics.core.PaludisPackage

/**
 * Created by durov_an on 07.04.2016.
 */

class PaludisPackageTest extends GroovyTestCase {
    PaludisPackage paludisPackage
//    String svnUsername = System.getProperty('domainUser')
//    char[] svnPassword = System.getProperty('domainPassword')

    PaludisPackageTest() {
//        paludisPackage = new PaludisPackage("tander-tsdserver", "tomcatsrv-rc-tsd", svnUsername, svnPassword)
    }

    void testGetBuildBySPPRTask() {
//        assert "1.0.103" == paludisPackage.getBuildBySPPRTask("SP0799306").version
        // нет коммита с такой задачей
//        assert null == paludisPackage.getBuildBySPPRTask("SP_abcd").version
        // последняя версия
//        assert "1.0.105" == paludisPackage.getBuildBySPPRTask(null).version
//        paludisPackage = new PaludisPackage("tander-tsdserver", "tomcatsrv-rc-web-gradle", svnUsername, svnPassword)
//        assert "1.0.0" == paludisPackage.getBuildBySPPRTask("SP0799306").version
    }

    void testGetSVNURL() {

    }
}
