UTDependencyChecker
===================

Tool to run only the relevant UT based on the changes between two git branches

The problem:

Running UT in android is slow, this is a problem when working with CI. 
This tool analyzes all classes in a folder (and subfolders, a complete compiled repository or project)
and creates a class file (DependencySuite.class) with a TestSuite to run only the tests relevant to 
the changes made between two branches.

Usage:

1- Run script check.sh BRANCH_MASTER BRANCH_WITH_CHANGES REPOSITORY_FOLDER TEST_PROJECT_CLASSES_FOLDER

be sure "color" is not enabled by default in your git config.

- BRANCH_MASTER: the master branch to compare with. Default is "devel".
- BRANCH_WITH_CHANGES: the branch with the new changes, the tests will be searched based on the changes in this branch. Dehault is "HEAD"
- REPOSITORY_FOLDER: the root folder of the repository to read the modified .java files. If not set uses current folder. Default is the folder where the script is running.
- TEST_PROJECT_CLASSES_FOLDER: the folder where the new class DependencySuite.class will be saved. If not set uses current folder. Default is the folder where the script is running.

3- Run the instrumentation command with the option "-e class DependencySuite"
e.g.: adb shell am instrument -w -e class DependencySuite com.android.foo/android.test.InstrumentationTestRunner

A good place to run this tool is in the target "-post-compile" of the test project, due to in this stage all the
java files in the project and the test project were compiled.

Known issues/limitations:
- to recognize a test it checks the class is not abstract, has at least one method annotated with @Test, or at least one method named "testXXX"
- hardcoded, the following packages and subpackages are ignored: "java", "android", "com", "org". 