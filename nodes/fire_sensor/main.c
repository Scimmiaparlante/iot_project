#include <stdlib.h>
#include <stdio.h>
#include "contiki.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"
#include "coap-constants.h"
#include "os/dev/button-hal.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "FIRE_SENSOR"
#define LOG_LEVEL LOG_LEVEL_INFO

#define REG_SEND_INTERVAL 		(10*CLOCK_SECOND)

#define PUBLISH_INTERVAL 		(20*CLOCK_SECOND)

// Server IP and resource path
#define SERVER_EP "coap://[fd00::1]:5683"

//---------------- I/O DEVICES -------------------------
button_hal_button_t* btn;
uint8_t n_button_press = 0;
//------------------------------------------------------


//---------------- RESOURCES LIST ----------------------
extern coap_resource_t firedetector_res;
//------------------------------------------------------


/* Declare and auto-start the server and registrator processses */
PROCESS(firedet_registration_process, "Registration process for fire detector");
PROCESS(server_process, "Server process");
AUTOSTART_PROCESSES(&firedet_registration_process, &server_process);


/* ---------------------------------------------------------------------------
 * ----------------------- REGISTRATION PROCESSES ----------------------------
 * -------------------------------------------------------------------------*/

//global variable to stop the main loop when the registration is completed
uint8_t registered_firedet = 0;


// Define a handler to handle the response from the server
void response_handler_firedet(coap_message_t* response) 
{
	LOG_INFO("Fire sensor - Handling registration response...\n");
	
	if(response == NULL) {
		LOG_INFO("Fire sensor - Request timed out\n");
		return;
	}

	if(response->code == CHANGED_2_04) {
		LOG_INFO("Fire sensor - Registration successful\n");
		registered_firedet = 1;
	} else if (response->code == BAD_REQUEST_4_00) {
		LOG_INFO("Fire sensor - Registration failed: BAD REQUEST\n");
	} else {
		LOG_INFO("Fire sensor - Registration failed: unknown error: %x\n", response->code);
	}
}


PROCESS_THREAD(firedet_registration_process, ev, data)
{
	static coap_endpoint_t server_ep;
	static coap_message_t request[1];
	static struct etimer periodic_timer;
	static char resource_path[] = "/remote_dir";
	
  	PROCESS_BEGIN();

	//timer to send the request again in case of failure
	etimer_set(&periodic_timer, REG_SEND_INTERVAL);

	// Populate the coap_endpoint_t data structure
	coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);

  	LOG_INFO("Contiki-NG coap client started\n");

	while(!registered_firedet) {
   		PROCESS_WAIT_EVENT_UNTIL(ev == PROCESS_EVENT_TIMER && data == &periodic_timer);

	  	// Prepare the message
	  	coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);
	  	coap_set_header_uri_path(request, resource_path);
	  	
	  	// Set the payload
	  	const char msg[] = "{\"a\" : \"register\" , \"t\" : \"sensor\" , \"st\" : \"fire\" , \"p\" : \"/firedet\"}";
	  	coap_set_payload(request, (uint8_t *)msg, sizeof(msg) -1);
	  	coap_set_header_content_format(request, APPLICATION_JSON);
	  	
	  	// Issue the request in a blocking manner 
	  	// The function returns when the request has been sent (ack received)
	  	LOG_INFO("Fire sensor - Issuing registration request...\n");
	  	COAP_BLOCKING_REQUEST(&server_ep, request, response_handler_firedet);
	  	
	  	etimer_reset(&periodic_timer);
	}

  	PROCESS_END();
}



/* ---------------------------------------------------------------------------
 * --------------------------- SERVER PROCESS --------------------------------
 * -------------------------------------------------------------------------*/

 
PROCESS_THREAD(server_process, ev, data) 
{
	static struct etimer periodic_timer;
	
  	PROCESS_BEGIN();

	//timer to send the request again in case of failure
	etimer_set(&periodic_timer, PUBLISH_INTERVAL);
	
	clock_init();
	srand(clock_time());
	
	button_hal_init();
	btn = button_hal_get_by_index(0);
	
	// Activation of a resource
	coap_activate_resource(&firedetector_res, "firedet");	

  	LOG_INFO("Coap server started!\n");

	while(1) {
   		PROCESS_WAIT_EVENT_UNTIL((ev == PROCESS_EVENT_TIMER && data == &periodic_timer) || ev == button_hal_press_event);

		if(ev == button_hal_press_event)
			n_button_press++;
		else
		  	firedetector_res.trigger();	  	
		  	
		etimer_reset(&periodic_timer);
	}

  	PROCESS_END();
}


