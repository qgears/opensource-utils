/* 
 *  pslash - a lightweight framebuffer splashscreen for embedded devices. 
 *
 *  Copyright (c) 2006 Matthew Allum <mallum@o-hand.com>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 */

//#include "psplash.h"

#define _GNU_SOURCE 1
#include <assert.h>
#include <errno.h>
#include <fcntl.h>
#include <limits.h>
#include <linux/fb.h>
#include <linux/kd.h>
#include <linux/vt.h>
#include <signal.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#if defined(__i386__) || defined(__alpha__)
#include <sys/io.h>
#endif
#include <sys/ioctl.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <sys/types.h>
#include <termios.h>
#include <unistd.h>
#include <linux/kd.h>
#include <linux/vt.h>
#include <sys/ioctl.h>

#define DEBUG alma


#if DEBUG
#define DBG(x, a...) \
   { printf ( __FILE__ ":%d,%s() " x "\n", __LINE__, __func__, ##a); }
#else
#define DBG(x, a...) do {} while (0)
#endif

#define UNUSED(x) (void)(x)

/* Globals, needed for signal handling */
static int ConsoleFd      = -1;
static int VTNum          = -1;
static int VTNumInitial   = -1;
static int Visible        =  1;

static void
vt_request (int sig)
{
	UNUSED(sig);
  DBG("mark, visible:%i", Visible);

  if (Visible)
    {
      /* Allow Switch Away */
      if (ioctl (ConsoleFd, VT_RELDISP, 1) < 0)
	perror("Error cannot switch away from console");
      Visible = 0;

      /* FIXME: 
       * We likely now want to signal the main loop as to exit	 
       * and we've now likely switched to the X tty. Note, this
       * seems to happen anyway atm due to select() call getting
       * a signal interuption error - not sure if this is really
       * reliable however. 
      */
    }
  else
    {
      if (ioctl (ConsoleFd, VT_RELDISP, VT_ACKACQ))
	perror ("Error can't acknowledge VT switch");
      Visible = 1;
      /* FIXME: need to schedule repaint some how ? */
    }
}

static void
psplash_console_ignore_switches (void)
{
  struct sigaction    act;
  struct vt_mode      vt_mode;
  
  if (ioctl(ConsoleFd, VT_GETMODE, &vt_mode) < 0)
    {
      perror("Error VT_SETMODE failed");
      return;
    }

  act.sa_handler = SIG_IGN;
  sigemptyset (&act.sa_mask);
  act.sa_flags = 0;
  sigaction (SIGUSR1, &act, 0);
  
  vt_mode.mode = VT_AUTO;
  vt_mode.relsig = 0;
  vt_mode.acqsig = 0;

  if (ioctl(ConsoleFd, VT_SETMODE, &vt_mode) < 0)
    perror("Error VT_SETMODE failed");
}

void
psplash_console_handle_switches (void)
{
 if ((ConsoleFd = open("/dev/tty", O_RDWR|O_NDELAY, 0)) < 0)
    {
      fprintf(stderr, "Error cannot open /dev/tty: %s\n", strerror(errno));
      return;
    }
  struct sigaction    act;
  struct vt_mode      vt_mode;
 
  if (ioctl(ConsoleFd, VT_GETMODE, &vt_mode) < 0)
    {
      perror("Error VT_SETMODE failed");
      return;
    }

  act.sa_handler = vt_request;
  sigemptyset (&act.sa_mask);
  act.sa_flags = 0;
  sigaction (SIGUSR1, &act, 0);
  
  vt_mode.mode   = VT_PROCESS;
  vt_mode.relsig = SIGUSR1;
  vt_mode.acqsig = SIGUSR1;

  if (ioctl(ConsoleFd, VT_SETMODE, &vt_mode) < 0)
    perror("Error VT_SETMODE failed");
  printf("Set up console switch is ready!\n");
}

void 
psplash_console_switch (void) 
{
  char           vtname[10];
  int            fd;
  struct vt_stat vt_state;

  if ((fd = open("/dev/tty0",O_WRONLY,0)) < 0)
    {
      perror("Error Cannot open /dev/tty0");
      return;
    }

  /* Find next free terminal */
  if ((ioctl(fd, VT_OPENQRY, &VTNum) < 0))
    {
      perror("Error unable to find a free virtual terminal");
      close(fd);
      return;
    }
  
  close(fd);
  
  sprintf(vtname,"/dev/tty%d", VTNum);

  if ((ConsoleFd = open(vtname, O_RDWR|O_NDELAY, 0)) < 0)
    {
      fprintf(stderr, "Error cannot open %s: %s\n", vtname, strerror(errno));
      return;
    }

  if (ioctl(ConsoleFd, VT_GETSTATE, &vt_state) == 0)
    VTNumInitial = vt_state.v_active;

  /* Switch to new free terminal */

  psplash_console_ignore_switches ();

  if (ioctl(ConsoleFd, VT_ACTIVATE, VTNum) != 0)
    perror("Error VT_ACTIVATE failed");
  
  if (ioctl(ConsoleFd, VT_WAITACTIVE, VTNum) != 0)
    perror("Error VT_WAITACTIVE failed\n");

  psplash_console_handle_switches ();
  
  if (ioctl(ConsoleFd, KDSETMODE, KD_GRAPHICS) < 0)
    perror("Error KDSETMODE KD_GRAPHICS failed\n");

  return;
}

void
psplash_console_reset (void)
{
  int              fd;
  struct vt_stat   vt_state;

  if (ConsoleFd < 0)
    return;

  /* Back to text mode */
  ioctl(ConsoleFd, KDSETMODE, KD_TEXT); 

  psplash_console_ignore_switches ();

  /* Attempt to switch back to initial console if were still active */
  ioctl (ConsoleFd, VT_GETSTATE, &vt_state);

  if (VTNum == vt_state.v_active)
    {
      if (VTNumInitial > -1)
        {
	  ioctl (ConsoleFd, VT_ACTIVATE, VTNumInitial);
	  ioctl (ConsoleFd, VT_WAITACTIVE, VTNumInitial);
	  VTNumInitial = -1;
        }
    }

  /* Cleanup */

  close(ConsoleFd); 

  if ((fd = open ("/dev/tty0", O_RDWR|O_NDELAY, 0)) >= 0)
    {
      ioctl (fd, VT_DISALLOCATE, VTNum);
      close (fd);
    }

  return;
}

