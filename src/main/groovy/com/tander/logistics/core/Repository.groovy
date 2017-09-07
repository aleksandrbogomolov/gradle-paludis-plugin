package com.tander.logistics.core

interface Repository {
    void findSetByPackage(String packageName, String packageVersion)
    void findSetByName(String setName, String setVersion)
    void findPackageByName(String packageName, String packageVersion)
}