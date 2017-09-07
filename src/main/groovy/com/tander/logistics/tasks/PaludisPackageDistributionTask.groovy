package com.tander.logistics.tasks

import com.tander.logistics.PaludisPackageExtension
import com.tander.logistics.core.PackageVersion
import com.tander.logistics.core.ScmFile
import com.tander.logistics.svn.SvnBranchAbstract
import com.tander.logistics.svn.SvnUtils
import org.apache.commons.io.FilenameUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.tmatesoft.svn.core.SVNCancelException
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.SVNNodeKind
import org.tmatesoft.svn.core.wc.*

/**
 * Created by durov_an on 01.04.2016.
 */
class PaludisPackageDistributionTask extends DefaultTask {

    PaludisPackageExtension ext
    SvnUtils svnUtils
    SvnBranchAbstract currBranch
    SvnBranchAbstract prevBranch
    PackageVersion packageVersion

    LinkedHashMap<String, List<String>> wildcards
    Map<String, Boolean> paludisPackages = new HashMap<>()

    PaludisPackageDistributionTask() {
        group = "distribution"
        description = "get package version "
        this.ext = project.extensions.tanderPaludis
        this.packageVersion = new PackageVersion()
    }

    void initSVN() {
        this.svnUtils = new SvnUtils(this.ext.user, this.ext.password.toCharArray())
        currBranch = new SvnBranchAbstract(svnUtils, null, null, null)
        prevBranch = new SvnBranchAbstract(svnUtils, null, null, null)

        if (ext.currUrl) {
            currBranch.url = ext.currUrl
        } else {
            currBranch.url = currBranch.getUrlFromFolder(project.projectDir.toString())
        }

        if (ext.currRevision) {
            currBranch.revision = SVNRevision.create(ext.currRevision as long)
        } else {
            currBranch.revision = SVNRevision.create(currBranch.getLastRevision() as long)
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

    @TaskAction
    void run() {
        initSVN()
        def changedFiles = getChangedFiles(new ArrayList<String>())
        for (file in changedFiles) {
            boolean isMatched = false
            for (Map.Entry<String, List<String>> entry : wildcards.entrySet()) {
                for (wildcard in entry.value)
                    if (FilenameUtils.wildcardMatch(file, wildcard as String)) {
                        paludisPackages.put(entry.key, true)
                        isMatched = true
                        break
                    }
                if (isMatched) {
                    break
                }
            }
        }

//        project.version = generatePackageVersion()
        generatePackageVersion()
        generateEbuild()
//        generateSetEbuild()
    }

    void generatePackageVersion() { // TODO Добавить проверку версии по паттерну
        def strings = currBranch.packageNameFromUrl.toString().split('-')
        if (currBranch.url.toString().contains('release') || currBranch.url.toString().contains('tags')) {
            packageVersion.isRelease = true
            packageVersion.version = strings.last()
        } else {
            packageVersion.isRelease = false
            packageVersion.version = "${strings.last()}.${strings[1].substring(2)}"
        }
//        return version
    }

    List<String> getChangedFiles(List<String> changedFiles) {
        ISVNDiffStatusHandler diffStatusHandler = new ISVNDiffStatusHandler() {
            ScmFile scmFile

            @Override
            void handleDiffStatus(SVNDiffStatus svnDiffStatus) throws SVNException {
                if (svnDiffStatus.getKind() == SVNNodeKind.FILE) {
                    scmFile = new ScmFile(svnDiffStatus.getPath())
                    scmFile.url = svnDiffStatus.getURL().toString()
                }
                changedFiles << svnDiffStatus.getPath()
            }
        }
        svnUtils.doDiffStatus(prevBranch.url,
                prevBranch.revision,
                currBranch.url,
                currBranch.revision,
                diffStatusHandler)

        return changedFiles
    }

    def generateEbuild() {
        def destinationDir = new File(project.buildDir, "ebuilds")
        if (!destinationDir.exists()) {
            destinationDir.mkdirs()
            println(destinationDir.exists())
        }
        paludisPackages.each { key, value ->
            if (value) {
                new File(destinationDir, "$ext.packageName-$key-${packageVersion.version}.ebuild").text = new File("template/$key").text
            }
        }
    }

    def generateSetEbuild() {
        def setUrl = "https://sources.corp.tander.ru/svn/real_out/pkg/repository/set/$ext.setName"
        svnUtils.doExport("$setUrl/tomcatsrv-rc-web-1.148.1000.ebuild", "$project.buildDir.path/tmp", SVNRevision.HEAD, new ISVNEventHandler() {
            @Override
            void handleEvent(SVNEvent event, double progress) throws SVNException {
                logger.lifecycle("Exporting file " + event.getFile().toString())
            }

            @Override
            void checkCancelled() throws SVNCancelException {
            }
        })
    }
}
