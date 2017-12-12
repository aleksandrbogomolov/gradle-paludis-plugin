package com.tander.logistics.svn

import com.tander.logistics.PaludisPackageExtension
import com.tander.logistics.ui.UiUtils
import org.gradle.api.InvalidUserDataException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.tmatesoft.svn.core.SVNErrorMessage
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.auth.ISVNAuthenticationProvider
import org.tmatesoft.svn.core.auth.SVNAuthentication
import org.tmatesoft.svn.core.auth.SVNPasswordAuthentication

/**
 * Created by durov_an on 08.02.2017.
 */
class AuthenticationProvider implements ISVNAuthenticationProvider {

    Logger logger

    PaludisPackageExtension ext

    AuthenticationProvider(PaludisPackageExtension ext) {
        logger = Logging.getLogger(this.class)
        this.ext = ext
    }

    @Override
    SVNAuthentication requestClientAuthentication(String kind,
                                                  SVNURL svnurl,
                                                  String realm,
                                                  SVNErrorMessage svnErrorMessage,
                                                  SVNAuthentication previousAuth,
                                                  boolean authMayBeStored) {
        Boolean isCanceled
        String scmPass

        ext.user = previousAuth.getUserName()
        if (ext.project.hasProperty("domainPassword")) {
            ext.password = ext.project.property("domainPassword")
        } else {
            (scmPass, isCanceled) = UiUtils.promptPassword(
                    "Please enter password to access $realm",
                    "Please enter password to access $realm \n for user ${ext.user}:")
            ext.password = scmPass
        }
        if (isCanceled && svnErrorMessage) {
            throw new InvalidUserDataException(" ${svnErrorMessage.getErrorCode().toString()} \n" +
                    "${svnErrorMessage.toString()} ")
        }

        SVNAuthentication svnAuthenticationNew = new SVNPasswordAuthentication(ext.user, ext.password, true)
        return svnAuthenticationNew
    }

    @Override
    int acceptServerAuthentication(SVNURL svnurl, String s, Object o, boolean b) {
        return ACCEPTED
    }
}
