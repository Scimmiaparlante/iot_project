#include <stdlib.h>
#include <stdio.h>
#include "contiki.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"
#include "coap-constants.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "NODE"
#define LOG_LEVEL LOG_LEVEL_INFO

#define REG_SEND_INTERVAL 			(3*CLOCK_SECOND)

#define TEMP_PUBLISH_INTERVAL 		(20*CLOCK_SECOND)

// Server IP and resource path
#define SERVER_EP "coap://[fd00::1]:5683"


//---------------- RESOURCES LIST ----------------------
extern coap_resource_t temperature_res;
extern coap_resource_t alarm_res;
//------------------------------------------------------


/* Declare and auto-start the server and registrator processses */
PROCESS(temp_registration_process, "Registration process for temperature");
PROCESS(alarm_registration_process, "Registration process for alarm");
PROCESS(server_process, "Server process");
AUTOSTART_PROCESSES(&temp_registration_process, &server_process, &alarm_registration_process);




/* ---------------------------------------------------------------------------
 * ----------------------- REGISTRATION PROCESSES ----------------------------
 * -------------------------------------------------------------------------*/

//global variable to stop the main loop when the registration is completed
uint8_t registered_temp = 0;


// Define a handler to handle the response from the server
void response_handler_temp(coap_message_t* response) 
{
	uint8_t resp_code;	
	
	LOG_INFO("Handling registration response...\n");
	
	if(response == NULL) {
		LOG_INFO("Request timed out\n");
		return;
	}


	//see if the operation was successful
	resp_code = response->code;

	if(resp_code == CHANGED_2_04) {
		LOG_INFO("Registration successful\n");
		registered_temp = 1;
	} else if (resp_code == BAD_REQUEST_4_00) {
		LOG_INFO("Registration failed: BAD REQUEST\n");
	} else {
		LOG_INFO("Registration failed: unknown error: %x\n", resp_code);
	}

	return;
}


PROCESS_THREAD(temp_registration_process, ev, data)
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

  	LOG_INFO("Contiki-NG coap client0 started\n");

	while(!registered_temp) {
   		PROCESS_WAIT_EVENT_UNTIL(ev == PROCESS_EVENT_TIMER && data == &periodic_timer);

	  	// Prepare the message
	  	coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);
	  	coap_set_header_uri_path(request, resource_path);
	  	
	  	// Set the payload
	  	const char msg[] = "{\"a\" : \"register\" , \"t\" : \"sensor\" , \"st\" : \"temperature\" , \"p\" : \"/temperature\"}";
	  	coap_set_payload(request, (uint8_t *)msg, sizeof(msg) -1);
	  	coap_set_header_content_format(request, APPLICATION_JSON);
	  	
	  	// Issue the request in a blocking manner 
	  	// The function returns when the request has been sent (ack received)
	  	LOG_INFO("Issuing registration request...\n");
	  	COAP_BLOCKING_REQUEST(&server_ep, request, response_handler_temp);
	  	
	  	etimer_reset(&periodic_timer);
	}

  	PROCESS_END();
}


//---------------------------------------------------------------------------------------------------

//global variable to stop the main loop when the registration is completed
uint8_t registered_alarm = 0;


// Define a handler to handle the response from the server
void response_handler_alarm(coap_message_t* response) 
{
	uint8_t resp_code;	
	
	LOG_INFO("Handling registration response...\n");
	
	if(response == NULL) {
		LOG_INFO("Request timed out\n");
		return;
	}


	//see if the operation was successful
	resp_code = response->code;

	if(resp_code == CHANGED_2_04) {
		LOG_INFO("Registration successful\n");
		registered_alarm = 1;
	} else if (resp_code == BAD_REQUEST_4_00) {
		LOG_INFO("Registration failed: BAD REQUEST\n");
	} else {
		LOG_INFO("Registration failed: unknown error: %x\n", resp_code);
	}

	return;
}


PROCESS_THREAD(alarm_registration_process, ev, data)
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

  	LOG_INFO("Contiki-NG coap client1 started\n");

	while(!registered_alarm) {
   		PROCESS_WAIT_EVENT_UNTIL(ev == PROCESS_EVENT_TIMER && data == &periodic_timer);

	  	// Prepare the message
	  	coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);
	  	coap_set_header_uri_path(request, resource_path);
	  	
	  	// Set the payload
	  	const char msg[] = "{\"a\" : \"register\" , \"t\" : \"actuator\" , \"st\" : \"alarm\" , \"p\" : \"/alarm\"}";
	  	coap_set_payload(request, (uint8_t *)msg, sizeof(msg) -1);
	  	coap_set_header_content_format(request, APPLICATION_JSON);
	  	
	  	// Issue the request in a blocking manner 
	  	// The function returns when the request has been sent (ack received)
	  	LOG_INFO("Issuing registration request...\n");
	  	COAP_BLOCKING_REQUEST(&server_ep, request, response_handler_alarm);
	  	
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
	coap_activate_resource(&alarm_res, "alarm");

  	LOG_INFO("Coap server started!\n");

	while(1) {
   		PROCESS_WAIT_EVENT_UNTIL(ev == PROCESS_EVENT_TIMER && data == &periodic_timer);

	  	temperature_res.trigger();	  	
	  	
	  	etimer_reset(&periodic_timer);
	}

  	PROCESS_END();
}


