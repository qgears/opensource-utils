This folder contains the target platform specific prebuilt binary dependencies of Q-Gears libpng connector (libqpng).

 * bin : contains runtime dependencies of libqpng
 * include and lib folders are required for compilation of libqpng.
 * win32 and win64 folders contain Makefiles for building libqpng 
 
If the binaries (bin and lib folder content) need to be updated, then they can be rebuilt from source on native environment, and then copy the output files here.

= Building libpng binaries on Windows 64 bit =

== Environment ==

I've used Windows 7 64 bit machine for building.

Make sure you do not use folder names that contain spaces as installation folder names!

The following dev tools are needed to create a build:
 * cygwin
  * install cygwin64 https://cygwin.com/install.html
  * mark following tools under 'Devel' category :
   * mingw64_x86_64-gcc-core
   * mingw64_x86_64-gcc-g++
   * gcc-core
   * gcc-g++
   * make
   * cmake
   * + dependencies
 * dependency walker (optional tool to test build dll-s)
  * http://www.dependencywalker.com/
  * 64bit version needed

Source code :
 * zlib (currently used version is 1.2.3)
  * https://sourceforge.net/projects/libpng/files/zlib/1.2.3/
 * libpng (currently used version 1.2.37)
  * https://sourceforge.net/projects/libpng/files/libpng12/older-releases/1.2.37/

Download the sourcecodes as zip, and extract them in the following directory structure:
 C:\munka\libpng_build\lpng1237 - containing libpng sources
 C:\munka\libpng_build\zlib123 - containing zlib sources
 
== Building zlib ==

Open cygwin and cd to zlib123 folder

```
 ./configure
 make install
```

Fix the line endings in configure script (replace \r-s with \n) if it fails.

This command will build and deploy zlib binaries into proper places that will be needed to compile libpng in cygwin.

=== Create zlib164.dll ===

'''Update''' : it seems that this file is not necessary, it is included into libpng1264.dll

Edit zlib123/win32/Makefile.gcc file:

 1. Find the variable SHAREDLIB and update it to : ```SHAREDLIB = zlib164.dll```
 1. Set CC = x86_64-w64-mingw32-gcc
 
Open cygwin and cd to zlib123 folder

```
make -f win32/Makefile.gcc
```

The build might fail with some errors on example codes, but the shared library (zlib164.dll) and the other necessary files will be created in zlib123 directory.

use 'clean' task if strange error messages occur to be sure you do not rely on some outdated compiler output. (`make clean`, or make -f <makefile> clean)

Check that the dll does not contain any error: open the file with dependency walker. You should not see any errors (like missing dependent library or incompatible CPU types )

== Create libpng1264.dll ==

Edit lpng1237/scripts/makefile.mingw file :
 * specify the library name we needed: SHAREDLIB=libpng$(MINGDLL)64.dll
 * set zlib directory (based on zlib make install console output)
```
# Where the zlib library and include files are located
ZLIBLIB= /usr/local/lib
ZLIBINC= /usr/local/include
```
 * Edit CC = x86_64-w64-mingw32-gcc
 
The open cygwin and cd lpng1237 folder.

Make sure, that the make install for zlib was successfully executed before next step)

``` 
make -f scripts/makefile.mingw
```

Everything should compile fine, and all dll and lib file will be created in  lpng1237 folder.

Check the dll with dependency walker.

= Windows 32 bit binaries =

The 32 bit binaries were downloaded from http://gnuwin32.sourceforge.net/packages/libpng.htm and http://gnuwin32.sourceforge.net/packages/zlib.htm as prebuilt binaries.

Probably they could be built from source similarly as described above.

= Includes =

The necessary include files of libpng and zlib was copied from source code.

Java includes were copied from JDK installation folder such as c:\Program Files\Java\jdk1.8.0_60\include\
(the original version of these files are taken from a Java 1.6 JDK, I do not check whether they was changed in newer JDK-s)

