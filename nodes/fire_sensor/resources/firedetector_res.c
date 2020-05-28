#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"
#include "coap-constants.h"
#include "os/dev/button-hal.h"

#include "sys/log.h"
#define LOG_MODULE "TEMP_RES"
#define LOG_LEVEL LOG_LEVEL_INFO


static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);


EVENT_RESOURCE(firedetector_res,
         "title=\"fire-detector\";rt=\"Text\"",
         res_get_handler,
         NULL,
         NULL,
         NULL, 
		 res_event_handler);


//------------------------------ I/O EMULATION ----------------------------------------------------

extern button_hal_button_t* btn;
extern uint8_t n_button_press;

static uint8_t readFireDetector() 
{
	static uint8_t fire_state = 0;

	//if button pressed, cycle through the states
	fire_state = (fire_state + n_button_press) % 2;
	n_button_press = 0;
		
	return fire_state;
}

//-------------------------- END I/O EMULATION -------------------------------------------------------


static uint8_t is_on_fire;

static void res_event_handler(void)
{
	//DO THINGS
	is_on_fire = readFireDetector();
	
	LOG_INFO("Notifying everyone\n");
	
    // Notify all the observers
    coap_notify_observers(&firedetector_res);
}


static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	int len;
	
	LOG_INFO("Handling get request\n");
	
	coap_set_header_content_format(response, APPLICATION_JSON);
	
	len = snprintf((char *)buffer, preferred_size, 
	"{\"bt\": \"%lu\", \"bn\" : \"firedet\", \"bu\" : \"\",\"e\" : [ {\"v\" : \"%d\"} ]}", 
																	clock_seconds(), is_on_fire);
	len = (preferred_size < len) ? preferred_size : len;
  
  	coap_set_payload(response, buffer, len);
}






