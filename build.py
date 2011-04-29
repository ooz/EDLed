#!/usr/bin/python
# coding: utf-8

import sys
from subprocess import *

def terminal(cmd):
    return Popen(cmd, stdout=PIPE).communicate()[0]
def system(cmd):
    call(cmd, shell=True)

CLASSPATH_ENTRY_PREFIX = "\t<classpathentry "
CLASSPATH_ENTRY_SUFFIX = "/>\n"
KIND_LIB = "lib"
PATH_ATTR_PREFIX = "path=\""
PATH_ATTR_SUFFIX = "\""

def readClasspathFile(path):
    f = open(path, "r")
    lines = f.readlines()
    f.close()
    classpathEntries = filter(lambda l: l.startswith(CLASSPATH_ENTRY_PREFIX) 
                                        and l.endswith(CLASSPATH_ENTRY_SUFFIX),
                              lines)
    purgedEntries = map(lambda e: e[len(CLASSPATH_ENTRY_PREFIX):-len(CLASSPATH_ENTRY_SUFFIX)],
                        classpathEntries)
    purgedEntries = filter(lambda e: e.split(" ")[0].find(KIND_LIB) != -1, purgedEntries)
    libs = map(lambda e: e.split(" ")[1][len(PATH_ATTR_PREFIX):-len(PATH_ATTR_SUFFIX)], purgedEntries)

    return libs

JAVAC_CLASSPATH_SEP = ":"

def createJavacClasspath(libs):
    return reduce(lambda lib, libs: lib + JAVAC_CLASSPATH_SEP + libs, libs)

BUILD_DIR   = "build/"
RELEASE_DIR = "release/"
SOURCE_DIR  = "src/"

PLUGIN_DIR = "plugin/"
RES_DIR = "res/"

EDLED_DIR     = "edled/"
EDLED_MAIN    = "Application.java"
STIMULUS_DIR  = "stimulus/"
STIMULUS_MAIN = "StimulusPlugin.java"
DESIGN_DIR    = "design/"
DESIGN_MAIN   = "DesignPlugin.java"

LIB_DIR       = "res/lib/"
JAR_EXT       = ".jar"

CLASSPATH_FILE = ".classpath"
MANIFEST_FILE = "MANIFEST.MF"

def build(libs, mainFile):
    terminal(["javac", 
              "-sourcepath",
              SOURCE_DIR,
              "-classpath",
              createJavacClasspath(libs),
              "-d",
              "build/",
              mainFile
              ])

# MAIN #
if __name__ == "__main__":
    libsWithPaths = readClasspathFile(CLASSPATH_FILE)

    # Build EDLed
    build(libsWithPaths, SOURCE_DIR + EDLED_DIR + EDLED_MAIN)

    # Build Stimuli
    build(libsWithPaths, SOURCE_DIR + STIMULUS_DIR + STIMULUS_MAIN)

    # Build Design
    build(libsWithPaths, SOURCE_DIR + DESIGN_DIR + DESIGN_MAIN)

    system("echo \"pushd ./;cd build/;jar cmf ../MANIFEST.MF edled.jar edled/;jar cf stimulus.StimulusPlugin.jar stimulus/;jar cf design.DesignPlugin.jar design/;popd\" > createJARs.sh")
    system("bash createJARs.sh")
    system("rm createJARs.sh")

    # TODO
    #  * create directory structure
    #  * copy res, copy libs
    #  * place plugins
    #  * cleanup

