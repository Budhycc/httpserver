@echo off

IF EXIST classes. (
RMDIR /S /Q classes
)

javac -Xlint:deprecation -d classes -cp "lib/*" src\*.java

xcopy resource classes /E