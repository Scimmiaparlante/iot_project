#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"
#include "coap-constants.h"
#include "os/dev/button-hal.h"

#include "sys/log.h"
#define LOG_MODULE "TEMP_RES"
#define LOG_LEVEL LOG_LEVEL_INFO

#define RAND_SIGN 	(rand() % 2 == 0) ? -1 : 1)


static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void simulation_res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);


EVENT_RESOURCE(temperature_res,
         "title=\"temperature\";rt=\"Text\"",
         res_get_handler,
         simulation_res_post_handler,	//this resource is used to emulate the effect of the air-cond system on temperature
         NULL,
         NULL, 
		 res_event_handler);


//------------------------------ I/O EMULATION ----------------------------------------------------

extern button_hal_button_t* btn;
extern uint8_t n_button_press;

enum {NORMAL, HOT, COLD} temp_force_state = NORMAL;

//aircond effect simulation
uint8_t aircond_state = 0;

static float readTemperature() 
{
	static float temp = 20;

	//if button pressed, cycle through the states
	if (n_button_press > 0)		
		temp_force_state = (temp_force_state + n_button_press) % 3;
		
	if (temp_force_state == HOT)
		temp = 50;
	else if (temp_force_state == COLD)
		temp = 0;
		
	//the effect of the button push has been propagated, reset to normal
	temp_force_state = NORMAL;
	n_button_press = 0;
		
	temp = ( temp + (RAND_SIGN * ( ((float)(rand() % 10)) / 10 ) );
	
	//--- simulate the effect of the air conditioning system (+/- 10 degrees <==> 0.5 °C/second)
	if (aircond_state == 2)
		temp -= 10;
	else if (aircond_state == 1)
		temp += 10;
	//-----------------------------
		
	return temp;
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


static void simulation_res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	size_t len = 0;
	const char* msg_ptr;

	LOG_INFO("Handling post request - fake temperature effect\n");
	
	coap_set_header_content_format(response, TEXT_PLAIN);
	response->code = BAD_REQUEST_4_00;
	
	//retrieve message
	len = coap_get_post_variable(request, "cmd", &msg_ptr);
	if(len == 1) {
		if (*msg_ptr == '2')			//COLD
			aircond_state = 2;
		else if (*msg_ptr == '1')		//HOT
			aircond_state = 1;
		else if (*msg_ptr == '0')		//OFF
			aircond_state = 0;

		response->code = CHANGED_2_04;
	}
	
	LOG_INFO("The temp sensor got to know the air-cond system is in state %u\n", aircond_state);
}


