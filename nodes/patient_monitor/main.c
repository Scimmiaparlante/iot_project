#include <stdlib.h>
#include <stdio.h>
#include "contiki.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"
#include "coap-constants.h"
#include "os/dev/serial-line.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "PAT_MONITOR"
#define LOG_LEVEL LOG_LEVEL_INFO

#define REG_SEND_INTERVAL 		(3*CLOCK_SECOND)

#define PUBLISH_INTERVAL 		(3*CLOCK_SECOND)

// Server IP and resource path
#define SERVER_EP "coap://[fd00::1]:5683"


//---------------- RESOURCES LIST ----------------------
extern coap_resource_t heartbeat_res;
extern coap_resource_t bloodpressure_res;
//------------------------------------------------------


/* Declare and auto-start the server and registrator processses */
PROCESS(heartbeat_registration_process, "Registration process for heartbeat");
PROCESS(bloodpressure_registration_process, "Registration process for blood pressure");
PROCESS(server_process, "Server process");
AUTOSTART_PROCESSES(&heartbeat_registration_process, &server_process, &bloodpressure_registration_process);


/* ---------------------------------------------------------------------------
 * ----------------------- REGISTRATION PROCESSES ----------------------------
 * -------------------------------------------------------------------------*/

//global variable to stop the main loop when the registration is completed
static uint8_t registered_heartbeat = 0;


// Define a handler to handle the response from the server
void response_handler_heartbeat(coap_message_t* response) 
{
	LOG_INFO("Heartbeat sensor - Handling registration response...\n");
	
	if(response == NULL) {
		LOG_INFO("Heartbeat sensor - Request timed out\n");
		return;
	}

	if(response->code == CHANGED_2_04) {
		LOG_INFO("Heartbeat sensor - Registration successful\n");
		registered_heartbeat = 1;
	} else if (response->code == BAD_REQUEST_4_00) {
		LOG_INFO("Heartbeat sensor - Registration failed: BAD REQUEST\n");
	} else {
		LOG_INFO("Heartbeat sensor - Registration failed: unknown error: %x\n", response->code);
	}
}


PROCESS_THREAD(heartbeat_registration_process, ev, data)
{
	static coap_endpoint_t server_ep;
	static coap_message_t request[1];
	static struct etimer periodic_timer;
	static char resource_path[] = "/remote_dir";
	static const char msg[] = "{\"a\" : \"register\" , \"t\" : \"sensor\" , \"st\" : \"heartbeat\" , \"p\" : \"/heartbeat\"}";
	
  	PROCESS_BEGIN();

	//timer to send the request again in case of failure
	etimer_set(&periodic_timer, REG_SEND_INTERVAL);

	// Populate the coap_endpoint_t data structure
	coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);

  	LOG_INFO("Contiki-NG coap client0 started\n");

	while(!registered_heartbeat) {
   		PROCESS_WAIT_EVENT_UNTIL(ev == PROCESS_EVENT_TIMER && data == &periodic_timer);

	  	// Prepare the message
	  	coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);
	  	coap_set_header_uri_path(request, resource_path);
	  	
	  	// Set the payload
	  	coap_set_payload(request, (uint8_t *)msg, sizeof(msg) -1);
	  	coap_set_header_content_format(request, APPLICATION_JSON);
	  	
	  	// Issue the request in a blocking manner 
	  	// The function returns when the request has been sent (ack received)
	  	LOG_INFO("Heartbeat sensor - Issuing registration request...\n");
	  	COAP_BLOCKING_REQUEST(&server_ep, request, response_handler_heartbeat);
	  	
	  	etimer_reset(&periodic_timer);
	}

  	PROCESS_END();
}

//--------------------------------------------------------------------------------------------------

//global variable to stop the main loop when the registration is completed
static uint8_t registered_bloodpressure = 0;

// Define a handler to handle the response from the server
void response_handler_bloodpressure(coap_message_t* response) 
{
	LOG_INFO("Blood pressure sensor - Handling registration response...\n");
	
	if(response == NULL) {
		LOG_INFO("Blood pressure sensor - Request timed out\n");
		return;
	}

	if(response->code == CHANGED_2_04) {
		LOG_INFO("Blood pressure sensor - Registration successful\n");
		registered_bloodpressure = 1;
	} else if (response->code == BAD_REQUEST_4_00) {
		LOG_INFO("Blood pressure sensor - Registration failed: BAD REQUEST\n");
	} else {
		LOG_INFO("Blood pressure sensor - Registration failed: unknown error: %x\n", response->code);
	}
}


PROCESS_THREAD(bloodpressure_registration_process, ev, data)
{
	static coap_endpoint_t server_ep;
	static coap_message_t request[1];
	static struct etimer periodic_timer;
	static char resource_path[] = "/remote_dir";
	const char msg[] = "{\"a\" : \"register\" , \"t\" : \"sensor\" , \"st\" : \"bloodpressure\" , \"p\" : \"/bloodpressure\"}";
	
  	PROCESS_BEGIN();

	//timer to send the request again in case of failure
	etimer_set(&periodic_timer, REG_SEND_INTERVAL);

	// Populate the coap_endpoint_t data structure
	coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);

  	LOG_INFO("Contiki-NG coap client0 started\n");

	while(!registered_bloodpressure) {
   		PROCESS_WAIT_EVENT_UNTIL(ev == PROCESS_EVENT_TIMER && data == &periodic_timer);

	  	// Prepare the message
	  	coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);
	  	coap_set_header_uri_path(request, resource_path);
	  	
	  	// Set the payload
	  	coap_set_payload(request, (uint8_t *)msg, sizeof(msg) -1);
	  	coap_set_header_content_format(request, APPLICATION_JSON);
	  	
	  	// Issue the request in a blocking manner 
	  	// The function returns when the request has been sent (ack received)
	  	LOG_INFO("Blood pressure sensor - Issuing registration request...\n");
	  	COAP_BLOCKING_REQUEST(&server_ep, request, response_handler_bloodpressure);
	  	
	  	etimer_reset(&periodic_timer);
	}

  	PROCESS_END();
}



/* ---------------------------------------------------------------------------
 * --------------------------- SERVER PROCESS --------------------------------
 * -------------------------------------------------------------------------*/

char serial_line_message[50] = "";


PROCESS_THREAD(server_process, ev, data) 
{
	static struct etimer periodic_timer;
	
  	PROCESS_BEGIN();

	//timer to send the request again in case of failure
	etimer_set(&periodic_timer, PUBLISH_INTERVAL);
	
	clock_init();
	srand(clock_time());
	
	// Activation of a resource
	coap_activate_resource(&heartbeat_res, "heartbeat");	
	coap_activate_resource(&bloodpressure_res, "bloodpressure");

  	LOG_INFO("Coap server started!\n");

	while(1) {
   		PROCESS_WAIT_EVENT_UNTIL((ev == PROCESS_EVENT_TIMER && data == &periodic_timer) || ev == serial_line_event_message);

		if(ev == serial_line_event_message) {
			if(strcpy(serial_line_message, data));
		}
		else {
	  		heartbeat_res.trigger();
	  		bloodpressure_res.trigger();
	  	} 	
	  	
	  	etimer_reset(&periodic_timer);
	}

  	PROCESS_END();
}


