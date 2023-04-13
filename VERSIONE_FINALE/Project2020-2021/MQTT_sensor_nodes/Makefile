CONTIKI_PROJECT = MQTT_sensor_nodes
all: $(CONTIKI_PROJECT)

PROJECT_SOURCEFILES += mysensor.c

CONTIKI = ../../

include $(CONTIKI)/Makefile.dir-variables
MODULES += $(CONTIKI_NG_APP_LAYER_DIR)/mqtt

-include $(CONTIKI)/Makefile.identify-target

MODULES_REL += arch/platform/$(TARGET)

include $(CONTIKI)/Makefile.include
