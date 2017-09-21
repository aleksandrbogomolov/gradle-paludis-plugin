package com.tander.logistics.svn

import com.tander.logistics.core.PackageVersion
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
        repository.setAuthenticationManager(authManager)
        repository.testConnection()
    }

    String getWorkingDirectoryUrl(String dirPath) {
        SVNWCClient svnwcClient = clientManager.getWCClient()
        SVNInfo svnInfo = svnwcClient.doInfo(new File(dirPath), SVNRevision.WORKING)
        return svnInfo.getURL().toString()
    }

    void doImportSetByPath(String repoUrl, String path, String filePath, PackageVersion packageVersion) throws SVNException {
        DAVRepositoryFactory.setup()
        SVNRepository repository
        def out = null
        def fos = new FileOutputStream(filePath)
        try {
            repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(repoUrl))
            repository.setAuthenticationManager(authManager)
            SVNNodeKind node = repository.checkPath(path, SVNRevision.HEAD.getNumber())
            if (node == SVNNodeKind.NONE && packageVersion.isRelease) {
                path = findPreviousSetVersion(repository, path)
            } else if (node == SVNNodeKind.NONE && !packageVersion.isRelease) {
                def chainPath = (path - ".ebuild").split("-")
                path = "${chainPath[0]}-${chainPath[1]}-${chainPath[2]}-${chainPath[3].substring(0, chainPath[3].lastIndexOf("."))}.ebuild"
            }
            out = new ByteArrayOutputStream()
            repository.getFile(path, SVNRevision.HEAD.getNumber(), new SVNProperties(), out)
            out.writeTo(fos)
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

    String findPreviousSetVersion(SVNRepository repository, String path) {
        String result = path
        def regex = ~/\d*\.\d*\.\d*/
        def dir = new ArrayList<SVNDirEntry>()
        repository.getDir(".", SVNRevision.HEAD.getNumber(), new SVNProperties(), dir)
        def ebuildNames = new ArrayList()
        ebuildNames.add(path)
        dir.each { f ->
            def name = f.getName() - ".ebuild"
            def chainName = name.split("-")
            if (regex.matcher(chainName.last()).matches()) {
                ebuildNames.add(name)
            }
        }
        ebuildNames.sort()
        ebuildNames.each { e ->
            if (e == path) {
                return result
            } else {
                result = e
            }
        }
    }
}
