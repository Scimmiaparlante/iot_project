#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"

#include "sys/log.h"
#define LOG_MODULE "HEARTBEAT_RES"
#define LOG_LEVEL LOG_LEVEL_INFO

#define RAND_SIGN 	((rand() % 2 == 0) ? -1 : 1)

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);


EVENT_RESOURCE(heartbeat_res,
         "title=\"heartbeat\";rt=\"application/json\"",
         res_get_handler,
         NULL,
         NULL,
         NULL, 
		 res_event_handler);
		 
//------------------------------ I/O EMULATION ----------------------------------------------------

extern char serial_line_message[50];
		 
static int readHeartBeat() 
{	
	static float hb = 65;
	
	//------------------- SERIAL LINE COMMUNICATION TO EMULATE VARIATIONS ---------
	// THE MESSAGES MUST BE IN THE FORM:  [hb|Mp|mp]=<value>
	serial_line_message[2] = '\0';
	uint8_t val = atoi(serial_line_message + 3);
	
	if (strcmp(serial_line_message, "hb") == 0) {
		hb = val;
		serial_line_message[0] = '\0';
	}
	//------------------------------------------------------------------------------
	
	hb += RAND_SIGN * ( ((float)(rand() % 100)) / 300 );

	return hb;
}

//-------------------------- END I/O EMULATION -------------------------------------------------------

float heartbeat[3];

static void res_event_handler(void)
{
	//3 fake measurements (we pretend these are the measurements of the last 3 seconds)
	heartbeat[0] = readHeartBeat();
	heartbeat[1] = readHeartBeat();
	heartbeat[2] = readHeartBeat();

	LOG_DBG("Notifying everyone\n");
	
    // Notify all the observers
    coap_notify_observers(&heartbeat_res);
}


static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	int len;
	
	LOG_DBG("Handling get request\n");
	
	coap_set_header_content_format(response, TEXT_PLAIN);
	
	len = snprintf((char *)buffer, preferred_size, 
			"{\"bt\": \"%lu\", \"bn\" : \"heartbeat\", \"bu\" : \"bpm\","
			"\"e\" : [ {\"v\" : \"%d\", \"t\" : \"-2\"}, {\"v\" : \"%d\", \"t\" : \"-1\"}, {\"v\" : \"%d\", \"t\" : \"0\"} ]}", 
			clock_seconds(), (int)heartbeat[0], (int)heartbeat[1], (int)heartbeat[2]);
			
	len = (preferred_size < len) ? preferred_size : len;
  
  	coap_set_payload(response, buffer, len);
}






