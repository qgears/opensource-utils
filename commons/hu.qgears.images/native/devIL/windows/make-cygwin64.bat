echo off
if exist "error.txt" del error.txt
echo Running cygwin's make from directory "%cd%"
REM for cygwin we must use / as path-separator
SET currdir=%cd:\=/%
REM running a bash on mintty terminal emulator, then executing cd and make
C:\cygwin64\bin\mintty /bin/bash -l -c 'cd %currdir%;make -f cygwin.mk ^>error.txt 2^>^&1'

REM error.txt indicates error
PING localhost -n 2 -w 500 >NUL
if exist "error.txt" (
	type "error.txt"
	PING localhost -n 2 -w 500 >NUL
	del error.txt
) 
echo on