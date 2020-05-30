#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"

#include "sys/log.h"
#define LOG_MODULE "BLOODPRESSURE_RES"
#define LOG_LEVEL LOG_LEVEL_INFO

#define RAND_SIGN 	((rand() % 2 == 0) ? -1 : 1)


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

extern char serial_line_message[50];

static struct bloodpressure_t readBloodpressure() 
{
	static struct bloodpressure_t pressure = {80,110};
	
	//------------------- SERIAL LINE COMMUNICATION TO EMULATE VARIATIONS ---------
	// THE MESSAGES MUST BE IN THE FORM:  [hb|Mp|mp]=<value>
	serial_line_message[2] = '\0';
	uint8_t val = atoi(serial_line_message + 3);
	
	if (strcmp(serial_line_message, "Mp") == 0) {
		pressure.max = val;
		serial_line_message[0] = '\0';
	}
	else if (strcmp(serial_line_message, "mp") == 0) {
		pressure.min = val;
		serial_line_message[0] = '\0';
	}
	//------------------------------------------------------------------------------
	
	pressure.max += RAND_SIGN * ( ((float)(rand() % 100)) / 100 );
	pressure.min += RAND_SIGN * ( ((float)(rand() % 100)) / 100 );
	
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

	LOG_DBG("Notifying everyone\n");
	
    // Notify all the observers
    coap_notify_observers(&bloodpressure_res);
}


static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	int len;
	
	LOG_DBG("Handling get request\n");
	
	coap_set_header_content_format(response, TEXT_PLAIN);
	
	
	len = snprintf((char *)buffer, preferred_size, 
			"{\"bt\": \"%lu\", \"bn\" : \"pressure\", \"bu\" : \"mmHg\","
			"\"e\" : [ {\"n\": \"Min\", \"v\" : \"%d\", \"t\" : \"-2\"}, {\"n\": \"Max\", \"v\" : \"%d\", \"t\" : \"-2\"}, {\"n\": \"Min\", \"v\" : \"%d\", \"t\" : \"-1\"},"
			"{\"n\": \"Max\", \"v\" : \"%d\", \"t\" : \"-1\"}, {\"n\": \"Min\", \"v\" : \"%d\", \"t\" : \"0\"}, {\"n\": \"Max\", \"v\" : \"%d\", \"t\" : \"0\"} ]}", 
			clock_seconds(), (int)pressure[0].min, (int)pressure[0].max, (int)pressure[1].min, (int)pressure[1].max, (int)pressure[2].min, (int)pressure[2].max);
			
	len = (preferred_size < len) ? preferred_size : len;
  
  	coap_set_payload(response, buffer, len);
}






