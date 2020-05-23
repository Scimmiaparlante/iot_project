#include <stdlib.h>
#include <stdio.h>
#include "contiki.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "SENSOR"
#define LOG_LEVEL LOG_LEVEL_INFO

#define REG_SEND_INTERVAL 			(3*CLOCK_SECOND)

#define TEMP_PUBLISH_INTERVAL 		(20*CLOCK_SECOND)

// Server IP and resource path
#define SERVER_EP "coap://[fd00::1]:5683"

//coap message codes
#define COAP_CREATED			((2U << 5) | 1U)
#define COAP_CHANGED			((2U << 5) | 4U)
#define COAP_BAD_REQUEST 		((4U << 5) | 0U)

#define COAP_APPLICATION_JSON	(50)

//---------------- RESOURCES LIST ----------------------
extern coap_resource_t temperature_res;
//------------------------------------------------------


/* Declare and auto-start the server and registrator processses */
PROCESS(registration_process, "Registration process");
PROCESS(server_process, "Server process");
AUTOSTART_PROCESSES(&registration_process, &server_process);




/* ---------------------------------------------------------------------------
 * ------------------------- REGISTRATION PROCESS ----------------------------
 * -------------------------------------------------------------------------*/

//global variable to stop the main loop when the registration is completed
uint8_t registered = 0;


// Define a handler to handle the response from the server
void response_handler(coap_message_t* response) 
{
	uint8_t resp_code;	
	
	LOG_INFO("Handling registration response...\n");
	
	if(response == NULL) {
		LOG_INFO("Request timed out\n");
		return;
	}


	//see if the operation was successful
	resp_code = response->code;

	if(resp_code == COAP_CHANGED) {
		LOG_INFO("Registration successful\n");
		registered = 1;
	} else if (resp_code == COAP_BAD_REQUEST) {
		LOG_INFO("Registration failed: BAD REQUEST\n");
	} else {
		LOG_INFO("Registration failed: unknown error: %x\n", resp_code);
	}

	return;
}


PROCESS_THREAD(registration_process, ev, data)
{
	static coap_endpoint_t server_ep;
	static coap_message_t request[1];
	static struct etimer periodic_timer;
	char resource_path[] = "/remote_dir";
	
  	PROCESS_BEGIN();

	//timer to send the request again in case of failure
	etimer_set(&periodic_timer, REG_SEND_INTERVAL);

	// Populate the coap_endpoint_t data structure
	coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);

  	LOG_INFO("Contiki-NG coap client started\n");

	while(!registered) {
   		PROCESS_WAIT_EVENT_UNTIL(ev == PROCESS_EVENT_TIMER && data == &periodic_timer);

	  	// Prepare the message
	  	coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);
	  	coap_set_header_uri_path(request, resource_path);
	  	
	  	// Set the payload
	  	const char msg[] = "{\"a\" : \"register\" , \"t\" : \"sensor\" , \"st\" : \"temperature\" , \"p\" : \"/temperature\"}";
	  	coap_set_payload(request, (uint8_t *)msg, sizeof(msg) -1);
	  	coap_set_header_content_format(request, COAP_APPLICATION_JSON);
	  	
	  	// Issue the request in a blocking manner 
	  	// The function returns when the request has been sent (ack received)
	  	LOG_INFO("Issuing registration request...\n");
	  	COAP_BLOCKING_REQUEST(&server_ep, request, response_handler);
	  	
	  	
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
	etimer_set(&periodic_timer, TEMP_PUBLISH_INTERVAL);
	
	// Activation of a resource
	coap_activate_resource(&temperature_res, "temperature");	

  	LOG_INFO("Coap server started!\n");

	while(1) {
   		PROCESS_WAIT_EVENT_UNTIL(ev == PROCESS_EVENT_TIMER && data == &periodic_timer);

	  	temperature_res.trigger();	  	
	  	
	  	etimer_reset(&periodic_timer);
	}

  	PROCESS_END();
}


