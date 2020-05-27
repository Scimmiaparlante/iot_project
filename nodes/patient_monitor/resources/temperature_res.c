#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"
#include "coap-constants.h"

#include "sys/log.h"
#define LOG_MODULE "TEMP_RES"
#define LOG_LEVEL LOG_LEVEL_INFO


static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);

/*
 * A handler function named [resource name]_handler must be implemented for each RESOURCE.
 * A buffer for the response payload is provided through the buffer pointer. Simple resources can ignore
 * preferred_size and offset, but must respect the REST_MAX_CHUNK_SIZE limit for the buffer.
 * If a smaller block size is requested for CoAP, the REST framework automatically splits the data.
 */
EVENT_RESOURCE(temperature_res,
         "title=\"temperature\";rt=\"Text\"",
         res_get_handler,
         NULL,
         NULL,
         NULL, 
		 res_event_handler);


int val = 0;

static void res_event_handler(void)
{
	//DO THINGS
	val = rand();
	
	LOG_INFO("Notifying everyone\n");
	
    // Notify all the observers
    coap_notify_observers(&temperature_res);
}


static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	int len;
	
	LOG_INFO("Handling get request\n");
	
	coap_set_header_content_format(response, APPLICATION_JSON);
	
	
	
	len = snprintf((char *)buffer, preferred_size, 
	"{\"bt\": \"%d\", \"bn\" : \"temperature\", \"bu\" : \"° C\",\"e\" : [ {\"v\" : \"%d\", \"t\" : \"0\"}, {\"v\" : \"%d\", \"t\" : \"0\"} ]}", 0,  val, val);
	len = (preferred_size < len) ? preferred_size : len;
  
  	coap_set_payload(response, buffer, len);
}






