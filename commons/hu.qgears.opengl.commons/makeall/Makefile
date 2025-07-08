# Compile all _new_ binaries that are used in current tests.

UBUNTU14 = $(shell grep "Ubuntu 14" /etc/issue)
DEBIAN10 = $(shell grep 'Debian GNU/Linux 10' /etc/issue)

all:
ifeq ($(strip $(UBUNTU14)),)
	make -C ../libinput
	make -C ../kms
	make -C ../kms_gl
endif

#Osmesa is only built on debian10 
all:
ifneq ($(strip $(DEBIAN10)),)
	make -C ../osmesa/linux
endif

