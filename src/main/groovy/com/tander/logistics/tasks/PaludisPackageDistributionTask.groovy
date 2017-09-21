package com.tander.logistics.tasks

import com.tander.logistics.PaludisPackageExtension
import com.tander.logistics.core.PackageVersion
import com.tander.logistics.core.ScmFile
import com.tander.logistics.svn.SvnBranchAbstract
import com.tander.logistics.svn.SvnUtils
import org.apache.commons.io.FilenameUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.SVNNodeKind
import org.tmatesoft.svn.core.wc.ISVNDiffStatusHandler
import org.tmatesoft.svn.core.wc.SVNDiffStatus
import org.tmatesoft.svn.core.wc.SVNRevision

/**
 * Created by durov_an on 01.04.2016.
 */
class PaludisPackageDistributionTask extends DefaultTask {

    String svnSetPath = 'https://sources.corp.tander.ru/svn/real_out/pkg/repository/set/'
    def parentEbuild = "$project.buildDir.path/parentEbuild.ebuild"

    PaludisPackageExtension ext
    SvnUtils svnUtils
    SvnBranchAbstract currBranch
    SvnBranchAbstract prevBranch
    PackageVersion packageVersion
    boolean doCheckSVN

    LinkedHashMap<String, List<String>> wildcards
    Map<String, Boolean> paludisPackages = new HashMap<>()
    Map<String, String> packages = new HashMap<>()

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
        if (doCheckSVN) {
            def changedFiles = getChangedFiles(new ArrayList<String>())
            for (file in changedFiles) {
                boolean isMatched = false
                for (Map.Entry<String, List<String>> entry : wildcards.entrySet()) {
                    for (wildcard in entry.value)
                        if (FilenameUtils.wildcardMatch(file, wildcard as String)) {
                            addToPaludisPackages(entry.key)
                            isMatched = true
                            break
                        }
                    if (isMatched) {
                        break
                    }
                }
            }
        } else {
            for (Map.Entry<String, List<String>> entry : wildcards.entrySet()) {
                addToPaludisPackages(entry.key)
            }
        }

        generatePackageVersion()
        generateEbuild()
        generateSetEbuild()
    }

    void addToPaludisPackages(String key) {
        paludisPackages.put(key, true)
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
        }
//        project.tasks.findByName("paludisPackageDistribution").property("paludisPackages") as HashMap
        paludisPackages.each { key, value ->
            if (value) {
                new File(destinationDir, "$ext.packageName-$key-${packageVersion.version}.ebuild").write(new File("template/$key").text, "UTF-8")
            }
        }
    }

    def generateSetEbuild() {
        svnUtils.doImportSetByPath("$svnSetPath$ext.setName", "$ext.setName-${packageVersion.version}.ebuild", parentEbuild, packageVersion)
        if (new File(parentEbuild).text == "") {
            new File(parentEbuild).text = new File("template/set").text
            wildcards.keySet().each { k ->
                addToPaludisPackages(k)
            }
        }
        doNewSetEbuild()
    }

    void doNewSetEbuild() {
        def packageList = new ArrayList<String>()
        wildcards.each { k, v ->
            packageList.add("$ext.setName-$k-$packageVersion.version")
        }
        paludisPackages.each { k, v ->
            packageList.each { tbz ->
                if (tbz.contains(k)) {
                    packages.put(k, tbz)
                }
            }
        }
        StringBuilder text = new StringBuilder("")
        new File(parentEbuild).eachLine { line ->
            text.append(checkLine(line))
        }
        new File("$project.buildDir.path/ebuilds/${ext.setName}.${packageVersion.version}.ebuild").write(text.toString(), 'UTF-8')
    }

    String checkLine(String line) {
        String result = "$line\n"
        packages.each { k, v ->
            if (line.contains(k)) {
                result = "${line.substring(0, line.indexOf('/') + 1)}$v\n"
            }
        }
        return result
    }
}
