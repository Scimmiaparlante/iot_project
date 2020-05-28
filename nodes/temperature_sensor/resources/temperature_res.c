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


EVENT_RESOURCE(temperature_res,
         "title=\"temperature\";rt=\"Text\"",
         res_get_handler,
         NULL,
         NULL,
         NULL, 
		 res_event_handler);


//------------------------------ I/O EMULATION ----------------------------------------------------

extern button_hal_button_t* btn;
extern uint8_t n_button_press;

enum {NORMAL, HOT, COLD} temp_state = NORMAL;

static float readTemperature() 
{
	static float old_temp = 20;
	float ret;

	//if button pressed, cycle through the states
	if (n_button_press > 0)		
		temp_state = (temp_state + n_button_press) % 3;
		
	n_button_press = 0;
		
	if (temp_state == NORMAL) {
		ret = ( old_temp + ((rand() % 2 == 0) ? -1 : 1) * ( ((float)(rand() % 100)) / 500 ) );
		old_temp = ret;
	}
	else if (temp_state == HOT)
		ret = 50;
	else
		ret = 0;
	
	return ret;
}

//-------------------------- END I/O EMULATION -------------------------------------------------------


static float temp;

static void res_event_handler(void)
{
	//DO THINGS
	temp = readTemperature();
	
	LOG_INFO("Notifying everyone\n");
	
    // Notify all the observers
    coap_notify_observers(&temperature_res);
}


static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	int len;
	
	LOG_INFO("Handling get request\n");
	
	coap_set_header_content_format(response, APPLICATION_JSON);
	
	
	len = snprintf((char *)buffer, preferred_size, 
	"{\"bt\": \"%lu\", \"bn\" : \"temperature\", \"bu\" : \"° C\",\"e\" : [ {\"v\" : \"%d\"} ]}", 
																	clock_seconds(), (int) temp);
	len = (preferred_size < len) ? preferred_size : len;
  
  	coap_set_payload(response, buffer, len);
}






