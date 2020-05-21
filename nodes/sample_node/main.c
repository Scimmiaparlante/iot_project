#include <stdlib.h>
#include <stdio.h>
#include "contiki.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "COAP"
#define LOG_LEVEL LOG_LEVEL_INFO

#define REG_SEND_INTERVAL (3*CLOCK_SECOND)

// Server IP and resource path
#define SERVER_EP "coap://[fd00::1]:9874"

//coap message codes
#define COAP_CREATED			((2U << 5) | 1U)
#define COAP_CHANGED			((2U << 5) | 4U)
#define COAP_BAD_REQUEST 		((4U << 5) | 0U)


/* Declare and auto-start this file's process */
PROCESS(main_process, "Main process");
AUTOSTART_PROCESSES(&main_process);


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


// The client includes two data structures
// coap_endpoint_t-> represents an endpoint
// coap_message_t-> represent the message
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(main_process, ev, data)
{
	static coap_endpoint_t server_ep;
	static coap_message_t request[1];  /* This way the packet can be treated as pointer as usual. */
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
	  	const char msg[] = "register";
	  	coap_set_payload(request, (uint8_t *)msg, sizeof(msg) -1);
	  	
	  	// Issue the request in a blocking manner 
	  	// The function returns when the request has been sent (ack received)
	  	COAP_BLOCKING_REQUEST(&server_ep, request, response_handler);
	  	
	  	
	  	etimer_reset(&periodic_timer);
	}

  	PROCESS_END();
}

