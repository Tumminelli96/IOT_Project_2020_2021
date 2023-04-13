/*
 * Copyright (c) 2006, Swedish Institute of Computer Science.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * This file is part of the Contiki operating system.
 *
 */

/**
 * \file
 *         A very simple Contiki application showing how Contiki programs look
 * \author
 *         Adam Dunkels <adam@sics.se>
 */

#include "contiki.h"
#include <string.h>
#include "os/dev/serial-line.h"
#include "os/dev/leds.h"
#include "mysensor.h"
#include <stdio.h> /* For printf() */
/*---------------------------------------------------------------------------*/
PROCESS(light_process, "light process");
AUTOSTART_PROCESSES(&light_process);
static int counter=0;
/*---------------------------------------------------------------------------*/

PROCESS_THREAD(light_process, ev, data)
{
  
  static char fractional_part[15];
  static struct etimer timer1;
  struct Sensor light;
  PROCESS_BEGIN();
  etimer_set(&timer1, CLOCK_SECOND * 2);
  while(1) 
  {
    PROCESS_YIELD();
    if(ev==PROCESS_EVENT_TIMER)
    {

      printf("counter value:%d\n",++counter);
      set_type_of_attuator(LIGHT_SYSTEM);
      light=read_lightness();
      printf("int light value%d\n:",(int)light.value())
      sprintf(fractional_part, "%d", (int)((light.value - (int)light.value + 1.0) * 1000000));
      printf("light: %d.%s LUX\n", (int)light.value,fractional_part[1]);
      printf("Actuator type: %d\n",light.tipe_of_attuator_active_on_sensor_area);
      etimer_reset(&timer1);   	
    }

  }

  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
