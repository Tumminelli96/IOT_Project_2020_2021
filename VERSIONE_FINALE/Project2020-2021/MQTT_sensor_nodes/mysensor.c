#include "mysensor.h"
#define SEED 13
static bool first_time_temp=false;
static bool first_time_light=false;
static bool random_initialized=false;
static struct Sensor light;
static struct Sensor temp;

float random_value(float min, float max)
{
	if(!random_initialized)
	{
		random_init(SEED);
		random_initialized=true;
	}
	float scale = random_rand() / (float) RANDOM_RAND_MAX;
 	return min + scale * (max - min);
}
//This function simulate a temperature reading by an actual temperature sensor
struct Sensor read_temperature()
{
	float new_value;
 	
 	if(first_time_temp==false)
 	{
  		strncpy(temp.name, "Temperature", 15);
		temp.value=random_value(0, 38);
		first_time_temp=true;
		temp.tipe_of_attuator_active_on_sensor_area = NONE;
 	}
	switch (temp.tipe_of_attuator_active_on_sensor_area) { 	
		case NONE: 
			new_value = temp.value + random_value(INF_TEMPERATURE_INTERVALL,SUP_TEMPERATURE_INTERVALL); 
			break;  
		case COOLING_SYSTEM: 
			new_value = temp.value + COOLING_TEMPERATURE; 
			break; 
		case HEATING_SYSTEM: 
			new_value = temp.value + HEATING_TEMPERATURE;
			printf("HEATYNG SYSTEM PATH"); 
			break;		
		default: 
			return temp;
			printf("Uknow type: %d",temp.tipe_of_attuator_active_on_sensor_area); 
			break; 
		}
	if(new_value < MAX_TEMPERATURE && new_value > MIN_TEMPERATURE)
	{
		temp.value = new_value;
	}
 	
 	return temp;
}
//This function simulate a light reading by an actual light sensor
struct Sensor read_lightness()
{
	float new_value;
	struct Sensor light_with_actuator;
 	if(first_time_light==false)
 	{
  		strncpy(light.name, "Lightness", 15);
		light.value=random_value(0, 2000);
		light.tipe_of_attuator_active_on_sensor_area = NONE;
		first_time_light=true;
		
 	}
 	new_value = light.value + random_value(INF_TEMPERATURE_INTERVALL,SUP_TEMPERATURE_INTERVALL);
	
	if(new_value < MAX_LIGHTNESS && new_value > MIN_LIGHTNESS)
	{
		
		light.value = new_value;
	}
 	if(light.tipe_of_attuator_active_on_sensor_area==LIGHT_SYSTEM)
		{
			strncpy(light_with_actuator.name, "Lightness", 15);
			light_with_actuator.value=light.value;
			if(light.value + LIGHT_INT < MAX_LIGHTNESS && light.value + LIGHT_INT > MIN_LIGHTNESS)
			{
				light_with_actuator.value=light.value + LIGHT_INT;
                        }
			else
			{
				light_with_actuator.value=MAX_LIGHTNESS;
			}
			light_with_actuator.tipe_of_attuator_active_on_sensor_area=light.tipe_of_attuator_active_on_sensor_area;
			return light_with_actuator;
		}
 	return light;
	
}
void set_type_of_attuator(uint8_t type)
{
	if(first_time_temp==false)
 	{
  		strncpy(temp.name, "Temperature", 15);
		temp.value=random_value(0, 38);
		first_time_temp=true;
		temp.tipe_of_attuator_active_on_sensor_area = NONE;
 	}
	if(first_time_light==false)
 	{
  		strncpy(light.name, "Lightness", 15);
		light.value=random_value(0, 1000);
		light.tipe_of_attuator_active_on_sensor_area = NONE;
		first_time_temp=true;
	}	
	temp.tipe_of_attuator_active_on_sensor_area=type;
	light.tipe_of_attuator_active_on_sensor_area=type;
}

void set_type_of_single_attuator(uint8_t type,uint8_t type_of_device)
{
	if(first_time_temp==false)
 	{
  		strncpy(temp.name, "Temperature", 15);
		temp.value=random_value(0, 38);
		first_time_temp=true;
		temp.tipe_of_attuator_active_on_sensor_area = NONE;
 	}
	if(first_time_light==false)
 	{
  		strncpy(light.name, "Lightness", 15);
		light.value=random_value(0, 1000);
		light.tipe_of_attuator_active_on_sensor_area = NONE;
		first_time_temp=true;
	}
	if(type_of_device==TEMP_DEVICE)	
	{
		temp.tipe_of_attuator_active_on_sensor_area=type;
	}
	else if(type_of_device==LIGHT_DEVICE)
	{
		light.tipe_of_attuator_active_on_sensor_area=type;
	}
}
