#Makefile for autobuild system, that only allows build on debian13
DEBIAN13 = $(shell grep 'Debian GNU/Linux 13' /etc/issue)
DEBIAN9 = $(shell grep 'Debian GNU/Linux 9' /etc/issue)


#Render libs are built only for debian13 
all:
ifneq ($(strip $(DEBIAN13)),)
	make -C ../libschrift/linux
	make -C ../stbtt/linux
else
ifneq ($(strip $(DEBIAN9)),)
	make -C ../libschrift/linux
	make -C ../stbtt/linux
else
	@echo "This build system is only supported on Debian 13 and 9. Exiting."
endif
endif

