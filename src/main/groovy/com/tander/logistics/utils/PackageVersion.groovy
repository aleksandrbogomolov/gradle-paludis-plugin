package com.tander.logistics.utils

/**
 * Created by durov_an on 06.04.2016.
 */
enum VersionType {
    Major,
    Minor,
    Release,
    Build
}

class PackageVersion {


    String version
    String versionMajor
    String versionMinor
    String versionRelease
    String versionBuild

    @Override
    String toString() {
        return "$versionMajor.$versionMinor.$versionRelease" + (versionBuild ? ".$versionBuild" : "")
    }

    def incVersion(VersionType versionType) {
        switch (versionType) {
            case VersionType.Major:
                versionMajor = versionMajor ? versionMajor + 1 : 1
                break
            case VersionType.Minor:
                versionMinor = versionMinor ? versionMinor + 1 : 1
                break
            case VersionType.Release:
                versionRelease= versionRelease ? versionRelease + 1 : 1
                break
            case VersionType.Build:
                versionBuild = versionBuild ? versionBuild + 1 : 1
                break
        }
    }
}
