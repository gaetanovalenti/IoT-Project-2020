#include "contiki.h"

#include <stdio.h>
#include <string.h>
#include "time.h"
#include "coap-engine.h"
#include "coap-observe.h"
#include "os/dev/leds.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_DBG

#define LOWER_THRESHOLD		200
#define UPPER_THRESHOLD		700

#define PERIODIC_HANDLER_INTERVAL 15

static int brightness_value = 450;
extern bool roller_state = 0;
static int counter = 0;

int generate_random_brightness(int lower, int upper) 
{ 
	int num = (rand() %  (upper - lower + 1)) + lower; 
	return num;
} 

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);

EVENT_RESOURCE(brightness,
   "title=\"Outside Brightness\";obs;rt=\"Brightness Sensor\"",
   res_get_handler,
   NULL,
   NULL,
   NULL,
	 res_event_handler);

static void res_event_handler(void)
{
	counter ++;
	coap_notify_observers(&brightness);
}

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  if(request != NULL) {
    LOG_DBG("Observing handler: %d\n", counter);
  }


  brightness_value = generate_random_brightness(0, 1030);

	if(brightness_value > UPPER_THRESHOLD){
		if(!roller_state){
			LOG_DBG("Automatic roller shutter system start, OPENED.\n");
			roller_state = 1;
			leds_set(LEDS_NUM_TO_MASK(LEDS_GREEN));
		}
	} else if(brightness_value < LOWER_THRESHOLD){
		if(roller_state){
			LOG_DBG("Automatic roller shutter system start, CLOSED.\n");
			roller_state = 0;
			leds_set(LEDS_NUM_TO_MASK(LEDS_RED));
		}
	}

  unsigned int accept = -1;
  coap_get_header_accept(request, &accept);
	
	if (accept == -1)
		accept = APPLICATION_JSON;

  if(accept == APPLICATION_XML) {
    coap_set_header_content_format(response, APPLICATION_XML);
    snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "<brightness=\"%d\"/>", brightness_value);
    coap_set_payload(response, buffer, strlen((char *)buffer));
    
  } else if(accept == APPLICATION_JSON) {
    coap_set_header_content_format(response, APPLICATION_JSON);
    snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"brightness\":%d}", brightness_value);
    coap_set_payload(response, buffer, strlen((char *)buffer));
    
  } else {
    coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
    const char *msg = "Supporting content-type application/json";
    coap_set_payload(response, msg, strlen(msg));
  }
}
