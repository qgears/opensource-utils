#!/bin/sh

BUILDDIR=$1
TARGETDIR=$2

rm -fr $BUILDDIR
mkdir -p $BUILDDIR
cp -r ../freeglut-2.6.0/* $BUILDDIR

cd $BUILDDIR
./configure
make clean
make

rm -fr $TARGETDIR
mkdir -p $TARGETDIR
cp $BUILDDIR/src/.libs/* $TARGETDIR/

