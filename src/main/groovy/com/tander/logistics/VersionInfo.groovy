package com.tander.logistics

import groovy.transform.Canonical

/**
 * Created by durov_an on 07.12.2016.
 */

@Canonical
public class VersionInfo {

    static final VersionInfo NONE = new VersionInfo()

    String scm = 'n/a'
    String branch = ''
    String branchType = ''
    String branchId = ''
    String commit = ''
    String display = ''
    String full = ''
    String base = ''
    String build = ''
    String tag = null
    boolean dirty = false
    boolean shallow = false

}