@echo off
dir /s /b *.java > file.lst
"c:\Program Files\Java\jdk1.8.0_152\bin\javadoc.exe" audioanalyzer -d ..\docs -javafx @file.lst
rm file.lst