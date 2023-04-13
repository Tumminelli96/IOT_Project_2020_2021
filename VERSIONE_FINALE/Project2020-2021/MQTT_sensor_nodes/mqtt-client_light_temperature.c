/*
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*---------------------------------------------------------------------------*/
#include "contiki.h"
#include "os/dev/serial-line.h"
#include "mysensor.h"
#include "net/routing/routing.h"
#include "mqtt.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/uip-icmp6.h"
#include "net/ipv6/sicslowpan.h"
#include "sys/etimer.h"
#include "sys/ctimer.h"
#include "lib/sensors.h"
#include "dev/button-hal.h"
#include "dev/leds.h"
#include "os/sys/log.h"
#include "mqtt-client_light_temperature.h"
#include <string.h>
#include <strings.h>
#include "os/dev/serial-line.h"
/*---------------------------------------------------------------------------*/
#define LOG_MODULE "mqtt-client"
#ifdef MQTT_CLIENT_CONF_LOG_LEVEL
#define LOG_LEVEL MQTT_CLIENT_CONF_LOG_LEVEL
#else
#define LOG_LEVEL LOG_LEVEL_DBG
#endif

/*---------------------------------------------------------------------------*/
/* MQTT broker address. */
#define MQTT_CLIENT_BROKER_IP_ADDR "fd00::1"
/* MQQTT broker address on local test*/

static const char *broker_ip = MQTT_CLIENT_BROKER_IP_ADDR;

// Default config values
#define DEFAULT_BROKER_PORT         1883
#define DEFAULT_PUBLISH_INTERVAL    (30 * CLOCK_SECOND)
#define HARD_CODED_ADDRESS      "d1c1::aa"

// We assume that the broker does not require authentication

/*---------------------------------------------------------------------------*/
/* Various states */
static uint8_t state;

#define STATE_INIT    	      0
#define STATE_NET_OK          1
#define STATE_CONNECTING      2
#define STATE_CONNECTED       3
#define STATE_SUBSCRIBED      4
#define STATE_DISCONNECTED    5

/*---------------------------------------------------------------------------*/
PROCESS_NAME(mqtt_client_light_temperature_process);
AUTOSTART_PROCESSES(&mqtt_client_light_temperature_process);

/*---------------------------------------------------------------------------*/
/* Maximum TCP segment size for outgoing segments of our socket */
#define MAX_TCP_SEGMENT_SIZE    32
#define CONFIG_IP_ADDR_STR_LEN   64
/*---------------------------------------------------------------------------*/
/*
 * Buffers for Client ID and Topics.
 * Make sure they are large enough to hold the entire respective string
 */
#define BUFFER_SIZE 64

static char client_id[BUFFER_SIZE];
static char pub_topic[BUFFER_SIZE];
static char sub_topic[BUFFER_SIZE];

/*---------------------------------------------------------------------------*/
/*Define the variable to setup the room where the sensor will be deployed*/
#define DEFAULT_ROOM "ROOM_A"
static char room[BUFFER_SIZE];
//static char *room = DEFAULT_ROOM;
static bool room_inizialized=false;
static bool sub_first_time=false;
static bool firs_sub_init=false;
static bool flag_publ=false;
// Periodic timer to check the state of the MQTT client
#define STATE_MACHINE_PERIODIC     (CLOCK_SECOND >> 1)
static struct etimer periodic_timer;

/*---------------------------------------------------------------------------*/
/*
 * The main MQTT buffers.
 * We will need to increase if we start publishing more data.
 */
#define APP_BUFFER_SIZE 1024
static char app_buffer[APP_BUFFER_SIZE];
/*---------------------------------------------------------------------------*/
static struct mqtt_message *msg_ptr = 0;

static struct mqtt_connection conn;

/*---------------------------------------------------------------------------*/
PROCESS(mqtt_client_light_temperature_process, "MQTT Client Light and Temperature");



/*---------------------------------------------------------------------------*/
/*
function used when an actuator command is published from the collector to the mqtt broker
*/
static void
pub_handler(const char *topic, uint16_t topic_len, const uint8_t *chunk,
            uint16_t chunk_len)
{
  int command=NONE;
  char topic_reg[BUFFER_SIZE];
  printf("Pub Handler: topic='%s' (len=%u), chunk_len=%u\n", topic,
          topic_len, chunk_len);
  sprintf(topic_reg,"actuator_light_%s",room);
  if(strcmp(topic, topic_reg) == 0) {
    printf("Received Actuator light command\n");
	printf("%s\n", chunk);
    if(memcmp(chunk, "0", 1) == 0)
    {
      command=NONE;
    }
    if(memcmp(chunk, "1", 1) == 0)
    {
      command=LIGHT_SYSTEM;
    }
    set_type_of_single_attuator(command,LIGHT_DEVICE);//used for simulation purpose to be changed
    return;
  }
  sprintf(topic_reg,"actuator_temperature_%s",room);
  if(strcmp(topic, topic_reg) == 0) {
    printf("Received Actuator temperature command\n");
	printf("%s\n", chunk);

    if(memcmp(chunk, "0", 1) == 0)
    {
      command=NONE;
    }
    if(memcmp(chunk, "1", 1) == 0)
    {
      command=HEATING_SYSTEM;
    }
    if(memcmp(chunk, "2", 1) == 0)
    {
      command=COOLING_SYSTEM;
    }
    set_type_of_single_attuator(command,TEMP_DEVICE);//used for simulation purpose to be changed
    return;
  }
}
/*---------------------------------------------------------------------------*/
static void
mqtt_event(struct mqtt_connection *m, mqtt_event_t event, void *data)
{
  switch(event) {
  case MQTT_EVENT_CONNECTED: {
    printf("Application has a MQTT connection\n");

    state = STATE_CONNECTED;
    break;
  }
  case MQTT_EVENT_DISCONNECTED: {
    printf("MQTT Disconnect. Reason %u\n", *((mqtt_event_t *)data));

    state = STATE_DISCONNECTED;
    process_poll(&mqtt_client_light_temperature_process);
    break;
  }
  case MQTT_EVENT_PUBLISH: {
    msg_ptr = data;

    pub_handler(msg_ptr->topic, strlen(msg_ptr->topic),
                msg_ptr->payload_chunk, msg_ptr->payload_length);
    break;
  }
  case MQTT_EVENT_SUBACK: {
#if MQTT_311
    mqtt_suback_event_t *suback_event = (mqtt_suback_event_t *)data;

    if(suback_event->success) {
      printf("Application is subscribed to topic successfully\n");
       sub_first_time=true;
    } else {
      printf("Application failed to subscribe to topic (ret code %x)\n", suback_event->return_code);
    }
#else
    printf("Application is subscribed to topic successfully\n");
    sub_first_time=true;
#endif
    break;
  }
  case MQTT_EVENT_UNSUBACK: {
    printf("Application is unsubscribed to topic successfully\n");
    break;
  }
  case MQTT_EVENT_PUBACK: {
    printf("Publishing complete.\n");
    break;
  }
  default:
    printf("Application got a unhandled MQTT event: %i\n", event);
    break;
  }
}

static bool
have_connectivity(void)
{
  if(uip_ds6_get_global(ADDR_PREFERRED) == NULL ||
     uip_ds6_defrt_choose() == NULL) {
    return false;
  }
  return true;
}

/*---------------------------------------------------------------------------*/
PROCESS_THREAD(mqtt_client_light_temperature_process, ev, data)
{
  struct Sensor light;
  struct Sensor temp;
  PROCESS_BEGIN();
  static mqtt_status_t status;
  static struct etimer timer_sample;
  
  static char broker_address[CONFIG_IP_ADDR_STR_LEN];
  sprintf(room, "%s", DEFAULT_ROOM);
  printf("MQTT Client Process\n");
  sub_first_time=false;
  // Initialize the ClientID as MAC address
  snprintf(client_id, BUFFER_SIZE, "%02x%02x%02x%02x%02x%02x",
                     linkaddr_node_addr.u8[0], linkaddr_node_addr.u8[1],
                     linkaddr_node_addr.u8[2], linkaddr_node_addr.u8[5],
                     linkaddr_node_addr.u8[6], linkaddr_node_addr.u8[7]);

  // Broker registration					 
  mqtt_register(&conn, &mqtt_client_light_temperature_process, client_id, mqtt_event,
                  MAX_TCP_SEGMENT_SIZE);
				  
  state=STATE_INIT;
  //Set the variable room with the value passed from serial line by the user
  while(!room_inizialized)
  {
    printf("Wait for the input\n");
    PROCESS_YIELD();
    printf("event detected:%d\n",ev);
    printf("event serial line:%d\n",serial_line_event_message);
    if(ev == serial_line_event_message) {
      printf("Check for the buffer size\n");
      if(strlen((char*)data)<BUFFER_SIZE - 1)
      {
        printf("Room correctly inizialized\n");
        sprintf(room,"%s",(char*)data);
        room_inizialized=true;
        printf("Room correctly inizialized:%s\n",room);
      }
      else
      {
        printf("The input is to large\n");
      }
    }
    
  }		    
  // Initialize periodic timer to check the status 
  etimer_set(&periodic_timer, STATE_MACHINE_PERIODIC);
  //inizialize periodic timer to sample light
  etimer_set(&timer_sample, CLOCK_SECOND * 1);
  /* Main loop */
  while(1) {

    PROCESS_YIELD();
    if((ev == PROCESS_EVENT_TIMER && data == &periodic_timer) || 
	      ev == PROCESS_EVENT_POLL){
			  			  
		  if(state==STATE_INIT){
			 if(have_connectivity()==true)  
				 state = STATE_NET_OK;
		  } 
		  
		  if(state == STATE_NET_OK){
			  // Connect to MQTT server
			  printf("Connecting!\n");
			  
			  memcpy(broker_address, broker_ip, strlen(broker_ip));
			  
			  mqtt_connect(&conn, broker_address, DEFAULT_BROKER_PORT,
						   (DEFAULT_PUBLISH_INTERVAL * 3) / CLOCK_SECOND,
						   MQTT_CLEAN_SESSION_ON);
			  state = STATE_CONNECTING;
		  }
		  
		  if(state==STATE_CONNECTED){
		  
			  // Subscribe to a topic
			  if(!sub_first_time&&!firs_sub_init)
			  {
			    sprintf(sub_topic,"actuator_light_%s",room);//the topic is: actuator_light_room_A

			    status = mqtt_subscribe(&conn, NULL, sub_topic, MQTT_QOS_LEVEL_0);

			    printf("Subscribing!\n");
			    if(status == MQTT_STATUS_OUT_QUEUE_FULL) {
				  LOG_ERR("Tried to subscribe but command queue was full!\n");
				  PROCESS_EXIT();
			    }
			   
			    firs_sub_init=true;
			  }

			  
			  if(sub_first_time)
			  {
				sprintf(sub_topic,"actuator_temperature_%s",room);//the topic is: actuator_temperature_room_A
                          	status = mqtt_subscribe(&conn, NULL, sub_topic, MQTT_QOS_LEVEL_0);
			  	printf("Subscribing at actuator_temperature_%s!\n",room);
			  	if(status == MQTT_STATUS_OUT_QUEUE_FULL) {
					LOG_ERR("Tried to subscribe but command queue was full !\n");
					PROCESS_EXIT();
			  	}
			        state = STATE_SUBSCRIBED;
                          }
			 
			  
			
		  }

			  
		 else if ( state == STATE_DISCONNECTED ){
		   LOG_ERR("Disconnected form MQTT broker\n");	
		   // Recover from error
		}
		
		etimer_set(&periodic_timer, STATE_MACHINE_PERIODIC);
      
    }
   if(ev == PROCESS_EVENT_TIMER && data == &timer_sample)
   {
     
     if(state == STATE_SUBSCRIBED && flag_publ){
       // Publish light
       sprintf(pub_topic, "%s", "light_status");
       light=read_lightness();
       printf("light: %f LUX\n", light.value);
       sprintf(app_buffer, "{\"room\": \"%s\",\"value\":%f,\"unit_of_measure\":\"LUX\"}",room,light.value);//post the sensed light as topic	
       mqtt_publish(&conn, NULL, pub_topic, (uint8_t *)app_buffer,
       strlen(app_buffer), MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);
       
      }
      if(state == STATE_SUBSCRIBED && !flag_publ){
	// Publish temperature
        sprintf(pub_topic, "%s", "temperature_status");
        temp=read_temperature();//read the temperature from the enviroment
        printf("temperature: %f C\n", temp.value);
        sprintf(app_buffer, "{\"room\": \"%s\",\"value\":%f,\"unit_of_measure\":\"C\"}",room,temp.value);
        //sprintf(app_buffer, "%s; %f",room,temp.value);//post the sensed temperature as topic	
        mqtt_publish(&conn, NULL, pub_topic, (uint8_t *)app_buffer,
        strlen(app_buffer), MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);
	
        }
      flag_publ= ! flag_publ;
      etimer_reset(&timer_sample);

   }
   
   
   if(ev == serial_line_event_message)
   {
      printf("stop session\n");
      if(strlen((char*)data)<BUFFER_SIZE - 1)
      {
        char comand[BUFFER_SIZE];
        sprintf(comand,"%s",(char*)data);
        if(strcmp("stop", comand) == 0)
        {
          mqtt_disconnect(&conn);
          printf("bye bye\n");
          break;
        }
        
      }
      else
      {
        printf("The input is to large\n");
      }
      
      
    }

  }

  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
