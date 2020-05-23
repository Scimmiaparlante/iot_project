CONTIKI_PROJECT = main
all: $(CONTIKI_PROJECT)
CONTIKI = ../../..

include $(CONTIKI)/Makefile.dir-variables

MODULES += $(CONTIKI_NG_APP_LAYER_DIR)/coap

CFLAGS += -DPROJECT_CONF_H=\"project-conf.h\"

# Include CoAP resources
MODULES_REL += ./resources

include $(CONTIKI)/Makefile.include
