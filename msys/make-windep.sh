#!/bin/sh

#Pre

#sudo apt install git

#Install packages
mkdir -p ./win-dep/usr/local/var/lib/pacman/

sudo pacman -r ./win-dep --config pacman.conf --noconfirm -Syuu

sudo pacman -r ./win-dep --config pacman.conf --noconfirm -S mingw32/mingw-w64-i686-pangomm 
sudo pacman -r ./win-dep --config pacman.conf --noconfirm -S mingw32/mingw-w64-i686-glew
sudo pacman -r ./win-dep --config pacman.conf --noconfirm -S mingw32/mingw-w64-i686-freeglut

sudo pacman -r ./win-dep --config pacman.conf --noconfirm -S mingw64/mingw-w64-x86_64-pangomm 
sudo pacman -r ./win-dep --config pacman.conf --noconfirm -S mingw64/mingw-w64-x86_64-glew
sudo pacman -r ./win-dep --config pacman.conf --noconfirm -S mingw64/mingw-w64-x86_64-freeglut

#pacman requires it to be root, now we can give the folders to the user
sudo chown -R `whoami` ./win-dep

#compile freeglut

cd freeglut
rm -rf freeglut-2.8.0
tar -zxf freeglut-2.8.0.tar.gz
make
rm -rf freeglut-2.8.0
cd ..

#clone thrid party git repositories

git clone https://github.com/meganz/mingw-std-threads.git win-dep/mingw-std-threads




