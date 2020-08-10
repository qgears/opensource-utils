#!/bin/bash

BUILDDIR=$1
TARGETDIR=$2


rm -fr $BUILDDIR
mkdir -p $BUILDDIR
cp -r ../freeglut-2.6.0/* $BUILDDIR

cd $BUILDDIR

# Upgrades old build scripts on Ubuntu 14.04 and above
#LSB_RELEASE=$(lsb_release -rs)
#if [ $(echo "$LSB_RELEASE >= 14.04" | bc -l) ]; then
	echo "Upgrading build scripts for Ubuntu 14 and above..." 
	autoreconf -vif
	echo "done $?"
#fi


./configure 

make clean
make -j8

rm -fr $TARGETDIR
mkdir -p $TARGETDIR
cp $BUILDDIR/src/.libs/* $TARGETDIR/

