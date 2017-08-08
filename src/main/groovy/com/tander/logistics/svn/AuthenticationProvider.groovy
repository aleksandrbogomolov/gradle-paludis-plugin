package com.tander.logistics.svn

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

    protected Logger logger

    AuthenticationProvider() {
        logger = Logging.getLogger(this.class)
    }

    @Override
    SVNAuthentication requestClientAuthentication(String kind,
                                                  SVNURL svnurl,
                                                  String realm,
                                                  SVNErrorMessage svnErrorMessage,
                                                  SVNAuthentication previousAuth,
                                                  boolean authMayBeStored) {
        Boolean isCanceled
        String scmUser
        String scmPass

        scmUser = previousAuth.getUserName()
        logger.lifecycle("Authentication error: " + svnErrorMessage)
        (scmPass, isCanceled) = UiUtils.promptPassword(
                "Please enter password to access $realm",
                "Please enter password to access $realm \n for user $scmUser:")

        if (isCanceled && svnErrorMessage) {
            throw new InvalidUserDataException(" ${svnErrorMessage.getErrorCode().toString()} \n" +
                    "${svnErrorMessage.toString()} ")
        }

        SVNAuthentication svnAuthenticationNew = new SVNPasswordAuthentication(scmUser, scmPass, true)
        return svnAuthenticationNew
    }

    @Override
    int acceptServerAuthentication(SVNURL svnurl, String s, Object o, boolean b) {
        return ACCEPTED
    }
}
