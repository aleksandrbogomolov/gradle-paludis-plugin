package com.tander.logistics.core

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Created by durov_an on 18.01.2017.
 */
abstract class ScmBranch implements IScmBranch {

    protected Logger logger
    String url
    String version
//    String revisionName
    ScmBranch() {
        logger = Logging.getLogger(this.class)
    }

    // считаем что структура директорий следующая
    // release/1.127.0/tsddispatcher    т.е. тип ветки / номер релиза / наименование пакета

    // или bracnches/user_name-sppr_number-description[-from_release_number]/tsddispatcher
    String[] getPathSegmentsFromUrl() {
        String[] segments = (new URL(url)).getPath().toString().split("/")
        return segments
    }

    String getPackageNameFromUrl() {
        String[] segments = getPathSegmentsFromUrl()
        return segments[segments.length - 1]
    }

    String getBranchTypeFromUrl() {
        String[] segments = getPathSegmentsFromUrl()
        return segments[segments.length - 3]
    }

    String getReleaseNumberFromUrl() {
        if (getBranchTypeFromUrl() == "releases") {
            String[] segments = getPathSegmentsFromUrl()
            return segments[segments.length - 2]
        }
    }
}
