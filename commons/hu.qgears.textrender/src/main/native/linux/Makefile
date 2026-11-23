#Makefile for autobuild system, that only allows build on debian13 arm64 and debian9 i386/amd64
DISTRO := $(strip $(shell . /etc/os-release 2>/dev/null; echo $$ID$$VERSION_ID))
# dpkg reports the userland arch (e.g. i386 in an i386 container on an x86_64 host), unlike uname -m
ARCH := $(strip $(shell dpkg --print-architecture 2>/dev/null))
PLATFORM := $(DISTRO)-$(ARCH)

all:
ifeq ($(PLATFORM),debian13-arm64)
	make -C ../libschrift/linux RELEASE=1 lib softfloatlib
else ifneq ($(filter debian9-i386 debian9-amd64,$(PLATFORM)),)
	make -C ../libschrift/linux RELEASE=1 lib softfloatlib checkdeps
else
	@echo "Build skipped on $(PLATFORM): only supported on debian13-arm64, debian9-i386 and debian9-amd64."
endif

