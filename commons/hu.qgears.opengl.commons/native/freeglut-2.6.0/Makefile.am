## Process this file with automake to produce Makefile.in

SUBDIRS = src include doc
AUTOMAKE_OPTIONS = gnu

EXTRA_DIST = \
	FrequentlyAskedQuestions \
	LISEZ_MOI \
	README.win32 \
	autogen.sh \
	freeglut.dep \
	freeglut.dsp \
	freeglut.dsw \
	freeglut.kdevelop \
	freeglut.kdevprj \
	freeglut.lsm \
	freeglut.mak \
	freeglut.rc \
	freeglut.spec \
	freeglut.tgt \
	freeglut.wpj \
	freeglut_evc4.vcp \
	freeglut_evc4.vcw \
	freeglut_static.dep \
	freeglut_static.dsp \
	freeglut_static.mak \
	freeglut_static.tgt \
	freeglut_static_vs2008.vcproj \
	freeglut_vs2008.sln \
	freeglut_vs2008.vcproj

# Thanks to WinDoze, some files in CVS have the "execute" bit set when they
# shoudn't. We could fix this if we had direct access to the CVS repository,
# but we are not that lucky. Instead we fix this at distribution creation time.
dist-hook:
	find $(distdir) -type f \( \
	    -name "*.c" -o \
	    -name "*.dsp" -o \
	    -name "*.dsw" -o \
	    -name "*.h" -o \
	    -name "*.html" -o \
	    -name "*.png" -o \
	    -name "LISEZ_MOI" -o \
	    -name "README*" \
	    \) -exec chmod -x {} \;

libtool: $(LIBTOOL_DEPS)
	$(SHELL) ./config.status --recheck
