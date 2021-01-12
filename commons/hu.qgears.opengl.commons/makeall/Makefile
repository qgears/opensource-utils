# Compile all _new_ binaries that are used in current tests.

UBUNTU14 = $(shell grep "Ubuntu 14" /etc/issue)

all:
ifeq ($(strip $(UBUNTU14)),)
	make -C ../libinput
	make -C ../kms
	make -C ../kms_gl
	make -C ../osmesa/linux
endif
