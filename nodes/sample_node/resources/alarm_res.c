#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"
#include "coap-constants.h"

#include "sys/log.h"
#define LOG_MODULE "ALARM_RES"
#define LOG_LEVEL LOG_LEVEL_INFO


static void res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

uint8_t alarm_on = 0;

RESOURCE(alarm_res,
         "title=\"alarm\";rt=\"Text\"",
         NULL,
         res_post_handler,
         NULL,
         NULL);



static void res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	LOG_INFO("Handling post request\n");
	
	coap_set_header_content_format(response, TEXT_PLAIN);
	response->code = CHANGED_2_04;
	
	size_t len = 0;
	const char* command;
	
	len = coap_get_post_variable(request, "cmd", &command);
	if(len == 1) {
		if (*command == '1')
			alarm_on = 1;
		else if (*command == '0')
			alarm_on = 0;
		else
			response->code = BAD_REQUEST_4_00;
	}
	
	LOG_INFO("alarm_on = %u\n", alarm_on);
}






