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

#ifndef _HAVE_PSPLASH_CONSOLE_H
#define _HAVE_PSPLASH_CONSOLE_H

/**
 * Switch to graphical mode.
 */
void psplash_console_switch (void);

/**
 * Is the VT currently active?
 */
bool psplash_is_active();
/**
 * Is a VT switch away request to deactivate currently active?
 */ 
bool psplash_is_req_away();
/**
 * Send acknowledge of VT switch away after we have finished deactivating our task on the screen and input.
 */
void psplash_away_ack(); 
/**
 * Is a VT switch back to activate request currently active?
 */ 
bool psplash_is_req_back(); 
/**
 * Send acknowledge of VT switch back after we have finished reactivating our task on the screen and input.
 */
void psplash_back_ack(); 

/**
 * On exit call this to reset the original state.
 */
void psplash_console_reset (void);

#endif

