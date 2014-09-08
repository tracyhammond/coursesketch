#!/usr/bin/python

import sys
import os
import shutil
import glob

def run():
    argNumber = len(sys.argv)
    if (argNumber > 1) :
        serverName = sys.argv[1]
        if query_yes_no("create new server named: " + serverName +"?", None):
            createServer(serverName)
        else :
            print "Restart with different name"
    else:
        print "ServerName Required!"
def createServer(serverName):
    print "Creating Directory"
    dir = serverName
    try:
        os.mkdir(serverName)
    except:
        if not query_yes_no("Directory Already Created Continue?"):
            return
    copyServerFiles(dir, serverName)
    createProjectFile(dir, serverName)
    createClassFile(dir, serverName)

def createProjectFile(dir,serverName):
    print "Creating Project File"
    fileName = dir + "/.project"
    with open(fileName, 'w') as f:
        total = """<?xml version="1.0" encoding="UTF-8"?>
<projectDescription>
    <name>""" + serverName + """</name>
    <comment></comment>
    <projects>
    </projects>
    <buildSpec>
        <buildCommand>
            <name>org.eclipse.jdt.core.javabuilder</name>
            <arguments>
            </arguments>
        </buildCommand>
    </buildSpec>
    <natures>
        <nature>org.eclipse.jdt.core.javanature</nature>
    </natures>
</projectDescription>
"""
        f.write(total)
def createClassFile(dir,serverName):
    print "Creating Class File"
    fileName = dir + "/.classpath"
    with open(fileName, 'w') as f:
        total = """<?xml version="1.0" encoding="UTF-8"?>
<classpath>
    <classpathentry kind="src" path="src"/>
    <classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>
    <classpathentry kind="lib" path="libs/java_websocket.jar"/>
    <classpathentry kind="lib" path="libs/websocketsource.jar"/>
    <classpathentry kind="lib" path="libs/protobuf-2.5.0.jar"/>
    <classpathentry kind="output" path="bin"/>
</classpath>
"""
        f.write(total)

def copyServerFiles(dir, serverName):
    print "Copying ServerFiles"
    os.system("cp -rf BlankServer/ " + serverName)
    shutil.rmtree(dir + '/bin')
    try:
        os.remove('.DS_Store')
    except:
        print "File Already Removed"
    try:
        os.remove(dir + '/.DS_Store')
    except:
        print "File Already Removed"

def query_yes_no(question, default="yes"):
    """Ask a yes/no question via raw_input() and return their answer.

    "question" is a string that is presented to the user.
    "default" is the presumed answer if the user just hits <Enter>.
        It must be "yes" (the default), "no" or None (meaning
        an answer is required of the user).

    The "answer" return value is one of "yes" or "no".
    """
    valid = {"yes":True,   "y":True,  "ye":True,
             "no":False,     "n":False}
    if default == None:
        prompt = " [y/n] "
    elif default == "yes":
        prompt = " [Y/n] "
    elif default == "no":
        prompt = " [y/N] "
    else:
        raise ValueError("invalid default answer: '%s'" % default)

    while True:
        sys.stdout.write(question + prompt)
        choice = raw_input().lower()
        if default is not None and choice == '':
            return valid[default]
        elif choice in valid:
            return valid[choice]
        else:
            sys.stdout.write("Please respond with 'yes' or 'no' "\
                             "(or 'y' or 'n').\n")

run()