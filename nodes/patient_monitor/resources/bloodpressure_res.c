#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"

#include "sys/log.h"
#define LOG_MODULE "BLOODPRESSURE_RES"
#define LOG_LEVEL LOG_LEVEL_INFO


static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);


EVENT_RESOURCE(bloodpressure_res,
         "title=\"bloodpressure\";rt=\"application/json\"",
         res_get_handler,
         NULL,
         NULL,
         NULL, 
		 res_event_handler);


struct bloodpressure_t {
	float min;
	float max;
};
		 
//------------------------------ I/O EMULATION ----------------------------------------------------
		 
static struct bloodpressure_t readBloodpressure() 
{
	static struct bloodpressure_t pressure = {80,110};
	
	//the first piece is to randomize the sign
	pressure.max = ( pressure.max + ((rand() % 2 == 0) ? -1 : 1) * ( ((float)(rand() % 100)) / 100 ) );
	pressure.min = ( pressure.min + ((rand() % 2 == 0) ? -1 : 1) * ( ((float)(rand() % 100)) / 100 ) );
	
	return pressure;
}

//-------------------------- END I/O EMULATION -------------------------------------------------------


struct bloodpressure_t pressure[3];


static void res_event_handler(void)
{
	//3 fake measurements (we pretend these are the measurements of the last 3 seconds)
	pressure[0] = readBloodpressure();
	pressure[1] = readBloodpressure();
	pressure[2] = readBloodpressure();

	LOG_INFO("Notifying everyone\n");
	
    // Notify all the observers
    coap_notify_observers(&bloodpressure_res);
}


static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	int len;
	
//	LOG_INFO("Handling get request\n");
	
	coap_set_header_content_format(response, TEXT_PLAIN);
	
	
	len = snprintf((char *)buffer, preferred_size, 
			"{\"bt\": \"%lu\", \"bn\" : \"pressure\", \"bu\" : \"mmHg\","
			"\"e\" : [ {\"n\": \"Min\", \"v\" : \"%d\", \"t\" : \"-2\"}, {\"n\": \"Max\", \"v\" : \"%d\", \"t\" : \"-2\"}, {\"n\": \"Min\", \"v\" : \"%d\", \"t\" : \"-1\"},"
			"{\"n\": \"Max\", \"v\" : \"%d\", \"t\" : \"-1\"}, {\"n\": \"Min\", \"v\" : \"%d\", \"t\" : \"0\"}, {\"n\": \"Max\", \"v\" : \"%d\", \"t\" : \"0\"} ]}", 
			clock_seconds(), (int)pressure[0].min, (int)pressure[0].max, (int)pressure[1].min, (int)pressure[1].max, (int)pressure[2].min, (int)pressure[2].max);
			
	len = (preferred_size < len) ? preferred_size : len;
  
  	coap_set_payload(response, buffer, len);
}






