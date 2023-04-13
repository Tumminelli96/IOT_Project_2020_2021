#include "contiki.h"
#include "coap-engine.h"
#include "dev/leds.h"

#include <string.h>

#if PLATFORM_HAS_LEDS || LEDS_COUNT

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP

static void res_post_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);
static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
/* A simple actuator example, depending on the color query parameter and post variable mode, corresponding led is activated or deactivated */
EVENT_RESOURCE(res_lights,
         "title=\"Light: ?POST/PUT mode=on|off\";rt=\"Control\"",
         res_get_handler,
         res_post_put_handler,
         res_post_put_handler,
         NULL,
         res_event_handler);
static int button = 0;
static void
res_post_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  size_t len = 0;
  const char *color = NULL;
  const char *mode = NULL;
  uint8_t led = 0;
  int success = 1;

  LOG_DBG("color %.*s\n", (int)len, color);
  led = LEDS_YELLOW;
  if(success && (len = coap_get_post_variable(request, "mode", &mode)))
  {
    LOG_DBG("mode %s\n", mode);
    if(strncmp(mode, "on", len) == 0) 
    {
      leds_on(LEDS_NUM_TO_MASK(led));
      button = 1;
    } else if(strncmp(mode, "off", len) == 0) 
      {
      leds_off(LEDS_NUM_TO_MASK(led));
      button = 0;
      } 
      else 
      {
        success = 0;
      }
  } else {
    success = 0;
  } if(!success) {
    coap_set_status_code(response, BAD_REQUEST_4_00);
  }
}
static void res_event_handler(void)
{
	LOG_DBG("start turn on light\n");
	uint8_t led = LEDS_YELLOW;
	if(button==0)
	{
                LOG_DBG("leds on\n");
		leds_on(LEDS_NUM_TO_MASK(led));
		button=1;
	}
	else
	{
		LOG_DBG("leds off\n");
  		leds_off(LEDS_NUM_TO_MASK(led));
		button=0;
	}
    // Notify all the observers
    coap_notify_observers(&res_lights);
}
static void
res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  coap_set_header_content_format(response, APPLICATION_JSON);
  LOG_DBG("{\"value\":%d}", button);
  coap_set_payload(response, buffer, snprintf((char *)buffer, preferred_size, "{\"value\":%d}", button));
}

#endif /* PLATFORM_HAS_LEDS */
