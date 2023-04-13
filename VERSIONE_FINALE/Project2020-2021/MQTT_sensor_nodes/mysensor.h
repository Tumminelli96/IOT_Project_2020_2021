/*---------------------------------------------------------------------------*/
#include <string.h>
#include <stdlib.h>
#include <stdint.h>
#include <stdbool.h>
#include<stdio.h>
#include"random.h"
#ifndef MYTEMP_H
#define MYTEMP_H
#define NOT_INITIALIZED 0
#define INITIALIZED 1
#define NONE 2
#define COOLING_SYSTEM 3
#define HEATING_SYSTEM 4
#define LIGHT_SYSTEM 5
#define INF_TEMPERATURE_INTERVALL -0.3
#define SUP_TEMPERATURE_INTERVALL 0.3
#define INF_LIGHTNESS_INTERVALL -2
#define SUP_LIGHTNESS_INTERVALL 2
#define MAX_LIGHTNESS 10000
#define MIN_LIGHTNESS 0
#define MAX_TEMPERATURE 65.0
#define MIN_TEMPERATURE -20.0
#define COOLING_TEMPERATURE -0.4
#define HEATING_TEMPERATURE 0.4
#define LIGHT_INT 1000
#define LIGHT_DEVICE 1
#define TEMP_DEVICE 2
struct Sensor {
 uint8_t tipe_of_attuator_active_on_sensor_area;
 char name[15];
 float value;
};
void set_type_of_attuator(uint8_t type);
void set_type_of_single_attuator(uint8_t type,uint8_t type_of_device);
struct Sensor read_temperature();
struct Sensor read_lightness();
#endif
/*---------------------------------------------------------------------------*/
