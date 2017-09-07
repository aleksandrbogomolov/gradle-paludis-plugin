package com.tander.logistics.svn

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.tmatesoft.svn.core.*
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager
import org.tmatesoft.svn.core.auth.ISVNAuthenticationProvider
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory
import org.tmatesoft.svn.core.io.SVNRepository
import org.tmatesoft.svn.core.io.SVNRepositoryFactory
import org.tmatesoft.svn.core.wc.*

/**
 * Created by durov_an on 01.04.2016.
 * для работы с SVN
 */
class SvnUtils {

    ISVNAuthenticationManager authManager
    SVNClientManager clientManager
    SVNRevision firstRevision
    Logger logger

    SvnUtils(String username, char[] password) {
        DAVRepositoryFactory.setup()
        ISVNAuthenticationProvider provider = new AuthenticationProvider()
        authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password)
        authManager.setAuthenticationProvider(provider)
        clientManager = SVNClientManager.newInstance(SVNWCUtil.createDefaultOptions(true), authManager)
        firstRevision = SVNRevision.create(1)
        logger = Logging.getLogger(this.class)
    }

    def doExport(String svnURL, String dirPath, SVNRevision revision, ISVNEventHandler dispatcher) {
        SVNUpdateClient updateClient = clientManager.getUpdateClient()
        updateClient.setEventHandler(dispatcher)
        updateClient.setIgnoreExternals(true)
        updateClient.doExport(
                SVNURL.parseURIEncoded(svnURL),
                new File(dirPath),
                revision,
                revision,
                '',
                false,
                SVNDepth.INFINITY
        )
    }

    def doCheckout(String svnURL, String dirPath, SVNRevision revision, ISVNEventHandler dispatcher) {
        SVNUpdateClient updateClient = clientManager.getUpdateClient()
        updateClient.setEventHandler(dispatcher)
        updateClient.setIgnoreExternals(true)
        updateClient.doCheckout(
                SVNURL.parseURIEncoded(svnURL),
                new File(dirPath),
                revision,
                revision,
                SVNDepth.INFINITY,
                false)
    }

    def doUpdate(String dirPath, SVNRevision revision, ISVNEventHandler dispatcher) {
        SVNUpdateClient updateClient = clientManager.getUpdateClient()
        updateClient.setEventHandler(dispatcher)
        updateClient.setIgnoreExternals(true)
        updateClient.doUpdate(
                new File(dirPath),
                revision,
                SVNDepth.INFINITY,
                false,
                false)
    }

    def doLog(String svnUrl, SVNRevision startRevision, SVNRevision endRevision, long limit, ISVNLogEntryHandler isvnLogEntryHandler) {
        SVNLogClient logClient = clientManager.getLogClient()
        logClient.doLog(
                SVNURL.parseURIEncoded(svnUrl),
                null,
                SVNRevision.UNDEFINED,
                startRevision,
                endRevision,
                true,
                true,
                limit,
                isvnLogEntryHandler)
    }

    def doDiffStatus(String prevSVNURL, SVNRevision prevSVNRevision, String curSVNURL, SVNRevision curSVNRevision, ISVNDiffStatusHandler diffStatusHandler) {
        SVNDiffClient diffClient = clientManager.getDiffClient()
        diffClient.doDiffStatus(
                SVNURL.parseURIEncoded(prevSVNURL),
                prevSVNRevision,
                SVNURL.parseURIEncoded(curSVNURL),
                curSVNRevision,
                SVNDepth.INFINITY,
                true,
                diffStatusHandler)
    }

    def doList(String svnURL, ISVNDirEntryHandler isvnDirEntryHandler) {
        SVNLogClient logClient = clientManager.getLogClient()
        logClient.doList(
                SVNURL.parseURIEncoded(svnURL),
                SVNRevision.HEAD,
                SVNRevision.HEAD,
                false,
                true,
                isvnDirEntryHandler)
    }

    void testConnection(String svnUrl) {
        SVNURL url = new SVNURL(svnUrl, true)
        SVNRepository repository = SVNRepositoryFactory.create(url, null);
        repository.setAuthenticationManager(authManager);
        repository.testConnection()
    }

    String getWorkingDirectoryUrl(String dirPath) {
        SVNWCClient svnwcClient = clientManager.getWCClient()
        SVNInfo svnInfo = svnwcClient.doInfo(new File(dirPath), SVNRevision.WORKING)
        return svnInfo.getURL().toString()
    }

    String getSomething(String path) {
        SVNWCClient svnwcClient = clientManager.getWCClient()
        SVNInfo svnInfo = svnwcClient.doInfo(new File(path), SVNRevision.WORKING)
        return svnInfo.repositoryRootURL.toString()
    }

    String getSvnFileByPath(String repoUrl, String path, String buildDir) throws SVNException {
        def tmpPath = "$buildDir/tmp"
        DAVRepositoryFactory.setup()
        SVNRepository repository = null
        def out = null
        def fos = new FileOutputStream(tmpPath)
        try {
            repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(repoUrl))
            repository.setAuthenticationManager(authManager)
            SVNNodeKind node = repository.checkPath(path, -1)
            if (node == SVNNodeKind.NONE) {
                logger.error("There is file at: $repoUrl $path.")
            } else if (node == SVNNodeKind.DIR) {
                logger.error("The entry at $repoUrl $path is a directory while a file was expected.")
            }
            out = new ByteArrayOutputStream()
            repository.getFile(path, -1, new SVNProperties(), out)
            out.writeTo(fos)
            return tmpPath
        } catch (SVNException e) {
            logger.error(e.errorMessage.toString())
        } finally {
            if (out != null) {
                out.close()
            }
            if (fos != null) {
                fos.close()
            }
        }
    }

    static SVNRevision getSvnRevision(String revision) {
        switch (revision) {
            case 'HEAD':
                return SVNRevision.HEAD
                break
            case 'WORKING':
                return SVNRevision.WORKING
                break
            case 'PREVIOUS':
                return SVNRevision.PREVIOUS
                break
            case 'BASE':
                return SVNRevision.BASE
                break
            case 'COMMITTED':
                return SVNRevision.COMMITTED
                break
            case 'UNDEFINED':
                return SVNRevision.UNDEFINED
                break
            default:
                return SVNRevision.create(revision as long)
                break
        }
    }
}
