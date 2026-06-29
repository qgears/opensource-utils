# Top-level Makefile for building both libschrift and stbtt libraries
# Uses make -C to delegate to subdirectory Makefiles

.PHONY: all clean libschrift stbtt

all: libschrift stbtt

clean:
	@echo "Cleaning libschrift..."
	$(MAKE) -C libschrift/linux clean
	@echo "Cleaning stbtt..."
	$(MAKE) -C stbtt/linux clean

libschrift:
	@echo "Building libschrift..."
	$(MAKE) -C libschrift/linux

stbtt:
	@echo "Building stbtt..."
	$(MAKE) -C stbtt/linux

# Alias for building all
build: all