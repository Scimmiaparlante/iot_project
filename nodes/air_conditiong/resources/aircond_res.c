#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"
#include "coap-constants.h"
#include "coap-callback-api.h"
#include "os/dev/leds.h"

#include "sys/log.h"
#define LOG_MODULE "AIR-COND_RES"
#define LOG_LEVEL LOG_LEVEL_INFO

#define ADDR_BUFFER_LEN			50


static void res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

//Things for the post request to temp sensor 
static void fakehandler(coap_callback_request_state_t *callback_state) {}
coap_callback_request_state_t req_state;

RESOURCE(aircond_res,
         "title=\"aircond\";rt=\"Text\"",
         NULL,
         res_post_handler,
         NULL,
         NULL);


uint8_t aircond_state = 0;

static void res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	size_t len = 0;
	const char* command;
	char origin[ADDR_BUFFER_LEN];			//ip address of the node originating the message

	LOG_INFO("Handling post request\n");
	
	coap_set_header_content_format(response, TEXT_PLAIN);
	response->code = BAD_REQUEST_4_00;
	
	memset(origin, 0, ADDR_BUFFER_LEN);	
	
	//retrieve message
	len = coap_get_post_variable(request, "cmd", &command);
	if(len == 1) {
		if (*command == '2')			//COLD
			aircond_state = 2;
		else if (*command == '1')		//HOT
			aircond_state = 1;
		else if (*command == '0')		//OFF
			aircond_state = 0;

		response->code = CHANGED_2_04;
	}
	
	//retrieve source ip
	len = coap_get_post_variable(request, "or", &command);
	if (len >= ADDR_BUFFER_LEN) {
		response->code = BAD_REQUEST_4_00;
		return;
	}
	memcpy(origin, command, len);
	
	//switch on/off the leds
	if(aircond_state == 0)
		leds_set(0);
	else
		leds_set(LEDS_NUM_TO_MASK(aircond_state-1));
		
	//--- now we need to emulate the effect of the air conditioner, telling he temp sensor to reduce the temp
	coap_endpoint_t server_ep;
	coap_message_t req[1];
  	char msg[] = "cmd=x";
  	
	coap_endpoint_parse(origin, strlen(origin), &server_ep);
  	coap_init_message(req, COAP_TYPE_CON, COAP_POST, 0);
  	coap_set_header_uri_path(req, "/temperature");
  	
	sprintf(msg, "cmd=%u", aircond_state);
  	coap_set_payload(req, (uint8_t *)msg, sizeof(msg) - 1);
  	coap_set_header_content_format(req, APPLICATION_JSON);
  	
  	coap_send_request(&req_state, &server_ep, req, fakehandler);
  	//---------------------------------------------------------------------------------
	
	LOG_INFO("aircond_state = %u\n", aircond_state);
	

}
