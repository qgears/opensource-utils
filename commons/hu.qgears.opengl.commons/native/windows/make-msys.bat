echo off
if exist "error.txt" del error.txt
if exist "out.txt" del out.txt
REM for cygwin we must use / as path-separator
SET currdir=%cd:\=/%
REM running a bash on mintty terminal emulator, then executing cd and make
echo Running msys2 make from directory "%cd%" using MINGW64 as MSYSTEM
set MSYSTEM=MINGW64
C:\msys64\usr\bin\mintty /bin/bash -l -c 'cd %currdir%;make -f msys.mk ^>out.txt 2^>error.txt'

REM error.txt indicates error
PING localhost -n 2 -w 300 >NUL

type out.txt
type error.txt
