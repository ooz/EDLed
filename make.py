#!/usr/bin/python
# coding: utf-8

import sys
from subprocess import *
from operator import *

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

EDLED_APP_DIR = "EDLed/"
EDLED_DIR     = "de/mpg/cbs/edled/"
EDLED_MAIN    = "Launcher.java"
STIMULUS_DIR  = "de/mpg/cbs/edledplugin/stimulus/"
STIMULUS_MAIN = "StimulusPlugin.java"
DESIGN_DIR    = "de/mpg/cbs/edledplugin/design/"
DESIGN_MAIN   = "DesignPlugin.java"

JAVA_PACKAGE_SEP = "."
FILE_SEP = "/"
JAR_EXT       = ".jar"
JAVA_EXT      = ".java"

PLUGINS = [ "de.mpg.cbs.edledplugin.stimulus.StimulusPlugin"
          , "de.mpg.cbs.edledplugin.design.DesignPlugin"
          ]

def pluginPackageDir(plugin):
    packageComps = plugin.split(JAVA_PACKAGE_SEP)[:-1]
    return reduce(lambda p, q: p + FILE_SEP + q, packageComps) + FILE_SEP

def pluginJar(plugin):
    return plugin + JAR_EXT

def createJarCMD(plugin):
    jar = pluginJar(plugin)
    packageDir = pluginPackageDir(plugin)
    return "jar cf %s %s;" % (jar, packageDir)

LIB_DIR       = "res/lib/"

CLASSPATH_FILE = ".classpath"
MANIFEST_FILE = "MANIFEST.MF"

def build(libs, mainFile):
    terminal(["javac", 
              "-sourcepath",
              BUILD_DIR,
              "-classpath",
              createJavacClasspath(libs),
              "-d",
              "build/",
              mainFile
              ])

# MAIN #
if __name__ == "__main__":
    print("Building EDLed")

    print(" Cleaning previous builds...")
    system("rm -rf " + BUILD_DIR)
    system("mkdir " + BUILD_DIR)

    system("cp -r " + SOURCE_DIR + "*" + " " + BUILD_DIR)

    print(" Extracting classpath from Eclipse classpath file.")
    libsWithPaths = readClasspathFile(CLASSPATH_FILE)

    print(" Compiling EDLed...")
    build(libsWithPaths, BUILD_DIR + EDLED_DIR + EDLED_MAIN)

    print(" Compiling Stimuli plugin...")
    build(libsWithPaths, BUILD_DIR + STIMULUS_DIR + STIMULUS_MAIN)

    print(" Compiling Design plugin...")
    build(libsWithPaths, BUILD_DIR + DESIGN_DIR + DESIGN_MAIN)

    print(" Packaging .jar archives...")
    pluginJarCMDs = "" + reduce(add, map(createJarCMD, PLUGINS))
    system("echo \"pushd ./;cd build/;jar cmf ../MANIFEST.MF edled.jar de/mpg/cbs/edled/;" + pluginJarCMDs + "popd\" > createJARs.sh")
    system("bash createJARs.sh")
    system("rm createJARs.sh")

    print(" Remove directory/packages containing the compiled .class files...")
    system("rm -rf build/de/")
#    system("rm -rf build/design/")
#    system("rm -rf build/edled/")
#    system("rm -rf build/stimulus/")

    print(" Copying ressource directory...")
    targetDir = BUILD_DIR + EDLED_APP_DIR
    system("mkdir -p " + targetDir)
    system("mkdir -p " + targetDir + PLUGIN_DIR)
    system("mkdir -p " + targetDir + RES_DIR)
    system("cp -r " + RES_DIR + "/* " + targetDir + RES_DIR)

    print(" Copying plugin directory...")
    system("cp -r " + PLUGIN_DIR + "/* " + targetDir + PLUGIN_DIR)

    print(" Moving main application JAR file...")
    system("mv -f " + BUILD_DIR + "edled.jar" + " " + targetDir + "edled.jar")

    print(" Moving plugin JAR files...")
    for plugin in PLUGINS:
        jar = pluginJar(plugin)
        system("mv -f " + BUILD_DIR + jar + " " + targetDir + PLUGIN_DIR + jar)

    print(" Copying various documentation files...")
    system("cp changelog.txt" + " " + targetDir)
    system("cp edled.sh" + " " + targetDir)

    # TODO
    #  * create directory structure
    #  * copy res, copy libs
    #  * place plugins
    #  * cleanup

    print("Build complete. Location: " + BUILD_DIR + EDLED_APP_DIR)
