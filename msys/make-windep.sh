#!/bin/bash
#
# Shell scrip that prepares win-deps folder
#
# Usage : package_editor.sh <https proxy> <http proxy>
#
#
HEAD_COMMENT_END=$(($LINENO-1)) #Marks the end of head comment. Used by function 'printhelp'. Please do not modify the position of this line in script!
function error {
	echo "$*"
	exit 1
}

function printHelp {
	head "-$HEAD_COMMENT_END" "$0"
}


function pinstall {
	$PACMAN_CMD -S "$1" || error "Cannot install package $1"
}

function createConf {
echo "
[options]

HoldPkg      = pacman
XferCommand = /usr/bin/wget --passive-ftp -c -O %o %u
Architecture = auto
CacheDir = ${PACMAN_CACHE}
CheckSpace

[mingw32]
Server = http://repo.msys2.org/mingw/i686

[mingw64]
Server = http://repo.msys2.org/mingw/x86_64
" > "${PACMAN_CONF}"

	cat "$PACMAN_CONF"
}


if [ $# -lt 2 ] ; then
	printHelp;
	error "Insufficient proxy conf!"
else 
	ROOT_DIR=`pwd`/win-dep
	PACMAN_CONF="$ROOT_DIR/pacman.conf"
	PACMAN_CACHE="$ROOT_DIR/cache/pacman/pkg/"
	PACMAN_CMD="pacman -r ${ROOT_DIR} --config ${PACMAN_CONF} --noconfirm"
	echo "Creating root dir $ROOT_DIR"
	mkdir -p "${ROOT_DIR}/usr/local/var/lib/pacman/"
	mkdir -p "${PACMAN_CACHE}"
	echo "Generating pacman.conf into $PACMAN_CONF"
	createConf

	export use_proxy=yes
	export https_proxy=$1
	export http_proxy=$2
	export ftp_proxy=$http_proxy
	export rsync_proxy=$http_proxy

	$PACMAN_CMD -Syuu || error "Failed to init package manager"


	
	pinstall mingw32/mingw-w64-i686-pangomm 
	pinstall mingw32/mingw-w64-i686-glew
	pinstall mingw32/mingw-w64-i686-freeglut

	pinstall mingw64/mingw-w64-x86_64-pangomm 
	pinstall mingw64/mingw-w64-x86_64-glew
	pinstall mingw64/mingw-w64-x86_64-freeglut

	#Override cairomm to an older version
	CAIRO_32="${PACMAN_CACHE}/mingw-w64-i686-cairomm-1.12.0-1-any.pkg.tar.xz"
	CAIRO_64="${PACMAN_CACHE}/mingw-w64-x86_64-cairomm-1.12.0-1-any.pkg.tar.xz"

	#wget http://repo.msys2.org/mingw/i686/mingw-w64-i686-cairomm-1.11.2-2-any.pkg.tar.xz -O "${CAIRO_32}" || "cannot download cairo 32"
	#wget http://repo.msys2.org/mingw/i686/mingw-w64-i686-cairomm-1.12.0-1-any.pkg.tar.xz -O "${CAIRO_32}" || "cannot download cairo 32"
	#wget http://repo.msys2.org/mingw/x86_64/mingw-w64-x86_64-cairomm-1.12.0-1-any.pkg.tar.xz -O "${CAIRO_64}" || "cannot download cairo 64"
	#wget http://repo.msys2.org/mingw/x86_64/mingw-w64-x86_64-cairomm-1.11.2-2-any.pkg.tar.xz -O "${CAIRO_64}" || "cannot download cairo 64"
	
	wget http://repo.msys2.org/mingw/i686/mingw-w64-i686-cairo-1.15.6-2-any.pkg.tar.xz -O "${CAIRO_32}" || "cannot download cairo 32"
	wget http://repo.msys2.org/mingw/x86_64/mingw-w64-x86_64-cairo-1.15.6-2-any.pkg.tar.xz -O "${CAIRO_64}" || "cannot download cairo 64"
	
	tar xpvf "${CAIRO_32}" -C ${ROOT_DIR} || "Cannot untart cairo 32"
	tar xpvf "${CAIRO_64}" -C ${ROOT_DIR} || "Cannot untart cairo 64"
	#compile freeglut

	cd freeglut
	rm -rf freeglut-2.8.0
	wget https://vorboss.dl.sourceforge.net/project/freeglut/freeglut/2.8.0/freeglut-2.8.0.tar.gz -O freeglut-2.8.0.tar.gz || error "Cannot download freeglut"
	tar -zxf freeglut-2.8.0.tar.gz
	make OUTDIR=${ROOT_DIR} || error "Cannot compile freeglut"
	rm -rf freeglut-2.8.0
	cd ..

	##Create JDK win include
	mkdir -p "${ROOT_DIR}/JDK-win"
	ln -s ../../../commons/hu.qgears.images/native/libpng/windows/deps/include/java/ ${ROOT_DIR}/JDK-win/include
fi






