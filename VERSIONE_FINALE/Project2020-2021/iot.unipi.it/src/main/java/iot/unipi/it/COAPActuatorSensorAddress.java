package iot.unipi.it;

public class COAPActuatorSensorAddress {
	private String room_name;
	private String presence_sensor_adress;
	private String temperature_actuator_address;
	private String light_actuator_address;
	

	public COAPActuatorSensorAddress(String room_name, String presence_sensor_adress,
			String temperature_actuator_address, String light_actuator_address) {
		this.room_name = room_name;
		this.presence_sensor_adress = presence_sensor_adress;
		this.temperature_actuator_address = temperature_actuator_address;
		this.light_actuator_address = light_actuator_address;
	}
	public String getRoom_name() {
		return room_name;
	}
	public void setRoom_name(String room_name) {
		this.room_name = room_name;
	}
	public String getLight_actuator_address() {
		return light_actuator_address;
	}
	public void setLight_actuator_address(String light_actuator_address) {
		this.light_actuator_address = light_actuator_address;
	}
	public String getTemperature_actuator_address() {
		return temperature_actuator_address;
	}
	public void setTemperature_actuator_address(String temperature_actuator_address) {
		this.temperature_actuator_address = temperature_actuator_address;
	}
	public String getPresence_sensor_adress() {
		return presence_sensor_adress;
	}
	public void setPresence_sensor_adress(String presence_sensor_adress) {
		this.presence_sensor_adress = presence_sensor_adress;
	}
	

}
