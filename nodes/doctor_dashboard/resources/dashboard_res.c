#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"
#include "coap-constants.h"

#include "sys/log.h"
#define LOG_MODULE "DASHBOARD_RES"
#define LOG_LEVEL LOG_LEVEL_INFO

#define BUFFER_LEN 				200
#define ADDR_BUFFER_LEN			50
#define MIN_INTERMESSAGE_TIME	20


static void res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);


RESOURCE(dashboard_res,
         "title=\"dashboard\";rt=\"Text\"",
         NULL,
         res_post_handler,
         NULL,
         NULL);



static void res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	static unsigned long last_message_time = -100;
	static char old_message[BUFFER_LEN] = "";
	
	char message[BUFFER_LEN];
	size_t len = 0;
	const char* msg_ptr;
	char origin[ADDR_BUFFER_LEN];			//ip address of the node originating the message

	LOG_DBG("Handling post request\n");
	
	coap_set_header_content_format(response, TEXT_PLAIN);
	response->code = CHANGED_2_04;
	
	memset(message, 0, BUFFER_LEN);
	memset(origin, 0, ADDR_BUFFER_LEN);	
	
	//retrieve message
	len = coap_get_post_variable(request, "msg", &msg_ptr);
	if (len >= BUFFER_LEN) {
		response->code = BAD_REQUEST_4_00;
		return;
	}
	
	memcpy(message, msg_ptr, len);
	
	//retrieve source ip
	len = coap_get_post_variable(request, "or", &msg_ptr);
	if (len >= ADDR_BUFFER_LEN) {
		response->code = BAD_REQUEST_4_00;
		return;
	}
	
	memcpy(origin, msg_ptr, len);
	
	//I don't display the same message twice consecutively. Wait MIN_INTERMESSAGE_TIME before showing again.
	//However, If two different messages are sent concurrently, I'll end up showing both muliple times.
	if(strcmp(message, old_message) != 0 || (clock_seconds() - last_message_time) > MIN_INTERMESSAGE_TIME) {
		LOG_WARN("DASHBOARD: %s - source: %s\n", message, origin);
		
		last_message_time = clock_seconds();
		strcpy(old_message, message);
	}

}
