#!/usr/bin/env bash

echo

SCRIPT_FOLDER=$(dirname "${BASH_SOURCE[0]}")
JAR=$SCRIPT_FOLDER/build/libs/dcheckut-1.0-SNAPSHOT.jar

if [ -f $JAR ]; then
    echo
    echo -------------------
    echo UTDependencyChecker
    echo -------------------
else
    echo
    echo -----------------------------------------------
    echo UTDependencyChecker jar not found, compiling...
    echo -----------------------------------------------
    gradle -p $SCRIPT_FOLDER jar
fi

master_branch=$1
if ["$1" == ""]; then
    master_branch=devel
fi

modified_branch=$2
if ["$2" == ""]; then
    modified_branch=HEAD
fi

echo
echo ----------------------------------------------------------
echo Comparing $master_branch..$modified_branch
echo ----------------------------------------------------------

echo
echo -------------------
echo Modified java files
echo -------------------
echo git log --name-only --pretty=%n $master_branch..$modified_branch
echo -------------------
git log --name-only --pretty=%n $master_branch..$modified_branch | sort -u

echo
echo ----------------------
echo Searching for tests...
echo ----------------------
git log --name-only --pretty=%n $master_branch..$modified_branch | java -jar $JAR . .