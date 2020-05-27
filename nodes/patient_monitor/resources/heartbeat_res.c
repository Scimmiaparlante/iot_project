#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"

#include "sys/log.h"
#define LOG_MODULE "HEARTBEAT_RES"
#define LOG_LEVEL LOG_LEVEL_INFO

#define MIME_APPLICATION_JSON	(50)

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);


EVENT_RESOURCE(heartbeat_res,
         "title=\"heartbeat\";rt=\"application/json\"",
         res_get_handler,
         NULL,
         NULL,
         NULL, 
		 res_event_handler);

		 
float heartbeat[3] = {65, 65, 65};		 
		 
static int readHeartBeat(int prev_measurement) 
{
	//the first piece is to randomize the sign
	return ( prev_measurement + ((rand() % 2 == 0) ? -1 : 1) * ( ((float)(rand() % 200)) / 100 ) );
}		 


static void res_event_handler(void)
{
	//3 fake measurements (we pretend these are the measurements of the last 3 seconds)
	heartbeat[0] = readHeartBeat(heartbeat[2]);
	heartbeat[1] = readHeartBeat(heartbeat[0]);
	heartbeat[2] = readHeartBeat(heartbeat[1]);

	LOG_INFO("Notifying everyone\n");
	
    // Notify all the observers
    coap_notify_observers(&heartbeat_res);
}


static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	int len;
	
//	LOG_INFO("Handling get request\n");
	
	coap_set_header_content_format(response, TEXT_PLAIN);
	
	
	len = snprintf((char *)buffer, preferred_size, 
			"{\"bt\": \"%lu\", \"bn\" : \"heartbeat\", \"bu\" : \"bpm\","
			"\"e\" : [ {\"v\" : \"%f\", \"t\" : \"-2\"}, {\"v\" : \"%f\", \"t\" : \"-1\"}, {\"v\" : \"%f\", \"t\" : \"0\"} ]}", 
			clock_seconds(), heartbeat[0], heartbeat[1], heartbeat[2]);
			
	len = (preferred_size < len) ? preferred_size : len;
  
  	coap_set_payload(response, buffer, len);
}






