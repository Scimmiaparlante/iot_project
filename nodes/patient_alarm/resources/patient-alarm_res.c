#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"
#include "coap-constants.h"
#include "os/dev/leds.h"

#include "sys/log.h"
#define LOG_MODULE "ALARM_RES"
#define LOG_LEVEL LOG_LEVEL_INFO

#define max(a,b) (((a) > (b)) ? (a) : (b))


static void res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

uint8_t patientalarm_level = 0;

RESOURCE(patientalarm_res,
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
	
	len = coap_get_post_variable(request, "lvl", &command);
	if(len == 1) {
		if (*command == '0')			//0 is a reset command
			patientalarm_level = 0;
		else if (*command == '1')		//for the other levels, I keep the highest value received since the last reset
			patientalarm_level = max(1, patientalarm_level);
		else if (*command == '2')
			patientalarm_level = max(2, patientalarm_level);
		else if (*command == '3')
			patientalarm_level = max(3, patientalarm_level);

		response->code = CHANGED_2_04;
	}
	
	if(patientalarm_level == 0)
		leds_set(0);
	else
		leds_set(LEDS_NUM_TO_MASK(patientalarm_level-1));
	
	LOG_INFO("patientalarm_on = %u\n", patientalarm_level);
}






