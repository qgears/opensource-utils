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


#ifdef DEBUG
#define DBG(x, a...) \
   { printf ( __FILE__ ":%d,%s() " x "\n", __LINE__, __func__, ##a); fflush(stdout);}
#else
#define DBG(x, a...) do {} while (0)
#endif

#define UNUSED(x) (void)(x)

/* Globals, needed for signal handling */
static int ConsoleFd      = -1;
static int VTNum          = -1;
static int VTNumInitial   = -1;
static int Visible        =  1;
static volatile bool reqAway = false;
static volatile bool reqBack = false;

static void psplash_console_handle_switches (bool onAway);
static void psplash_console_ignore_switches (void);


static void vt_request_away (int sig)
{
	UNUSED(sig);
	reqAway=true;
  DBG("VT REQ AWAY");
}
static void vt_request_back (int sig)
{
	UNUSED(sig);
	reqBack=true;
  DBG("VT REQ BACK");
}

bool psplash_is_active()
{
	return Visible!=0;
}
void psplash_away_ack()
{
      /* Allow Switch Away */
      if (ioctl (ConsoleFd, VT_RELDISP, 1) < 0)
      {
		perror("Error cannot switch away from console");
  }
      Visible = 0;
      reqAway=false;
      psplash_console_handle_switches(false);
      DBG("VT AWAY ACKED");
}

bool psplash_is_req_away()
{
 return reqAway;
}
bool psplash_is_req_back()
{
 return reqBack;
}

void psplash_back_ack()
{
    if (ioctl (ConsoleFd, VT_RELDISP, VT_ACKACQ))
    {
	  perror ("Error can't acknowledge VT switch");
    }
    Visible = 1;
    reqBack=false;
    psplash_console_handle_switches(true);
    DBG("VT BACK ACKED");
}

static void psplash_console_ignore_switches (void)
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

static void psplash_console_handle_switches (bool onAway)
{
  struct sigaction    act;
  struct vt_mode      vt_mode;
 
  if (ioctl(ConsoleFd, VT_GETMODE, &vt_mode) < 0)
    {
      perror("Error VT_GETMODE failed");
      return;
    }

  act.sa_handler = onAway? vt_request_away: vt_request_back;
  sigemptyset (&act.sa_mask);
  act.sa_flags = 0;
  sigaction (SIGUSR1, &act, 0);
  
  vt_mode.mode   = VT_PROCESS;
  vt_mode.relsig = onAway?SIGUSR1:0;
  vt_mode.acqsig = onAway?0:SIGUSR1;

  if (ioctl(ConsoleFd, VT_SETMODE, &vt_mode) < 0)
    perror("Error VT_SETMODE failed");
}

void psplash_console_switch (void) 
{
  char vtname[]="/dev/tty";
  if ((ConsoleFd = open(vtname, O_RDWR|O_NDELAY, 0)) < 0)
    {
      fprintf(stderr, "Error cannot open %s: %s\n", vtname, strerror(errno));
      return;
    }
  psplash_console_handle_switches (true);
  
  if (ioctl(ConsoleFd, KDSETMODE, KD_GRAPHICS) < 0)
    perror("Error KDSETMODE KD_GRAPHICS failed\n");

  DBG("Set up console switch is ready!\n");

  return;
}

void psplash_console_reset (void)
{
  int              fd;
  struct vt_stat   vt_state;

  if (ConsoleFd < 0)
    return;

  psplash_console_ignore_switches ();
  
  /* Back to text mode */
  ioctl(ConsoleFd, KDSETMODE, KD_TEXT); 


  /* Cleanup */
  close(ConsoleFd); 

  return;
}

