#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"
#include "coap-constants.h"
#include "os/dev/leds.h"

#include "sys/log.h"
#define LOG_MODULE "ALARM_RES"
#define LOG_LEVEL LOG_LEVEL_INFO


static void res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

uint8_t firealarm_on = 0;

RESOURCE(firealarm_res,
         "title=\"fire-alarm\";rt=\"Text\"",
         NULL,
         res_post_handler,
         NULL,
         NULL);



static void res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	LOG_INFO("Handling post request\n");
	
	coap_set_header_content_format(response, TEXT_PLAIN);
	response->code = BAD_REQUEST_4_00;
	
	size_t len = 0;
	const char* command;
	
	len = coap_get_post_variable(request, "cmd", &command);
	if(len == 1) {
		if (*command == '1')
			firealarm_on = 1;
		else if (*command == '0')
			firealarm_on = 0;

		response->code = CHANGED_2_04;
	}
	
	leds_set(firealarm_on * LEDS_NUM_TO_MASK(LEDS_CONF_RED));
	
	LOG_INFO("firealarm_on = %u\n", firealarm_on);
}






