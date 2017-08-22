package com.tander.logistics.core

interface Package {
    void saveToDisk()
    void getCRC()
    File getPackageFile()
    String getCurrentVersion()
    String getNewVersion()
}
