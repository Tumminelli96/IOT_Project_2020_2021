package iot.unipi.it;

import java.io.FileReader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class COAPActuatorSensorAddressList {
	private ConcurrentHashMap<String,COAPActuatorSensorAddress> coapList;
	public COAPActuatorSensorAddressList() 
	{
		coapList = new ConcurrentHashMap<String,COAPActuatorSensorAddress>();
		coapList.put("A", new COAPActuatorSensorAddress("A","[fd00::202:2:2:2]","[fd00::203:3:3:3]","[fd00::204:4:4:4]"));
	}
	public COAPActuatorSensorAddressList(String address) 
	{
		try
		{
			JSONParser parser = new JSONParser();
			JSONArray a = (JSONArray) parser.parse(new FileReader(address));
			coapList = new ConcurrentHashMap<String,COAPActuatorSensorAddress>();
			  for (Object o : a)
			  {
			    JSONObject addr_room = (JSONObject) o;

			    String room_name = (String) addr_room.get("room_name");
			    

			    String presence_sensor_adress = (String) addr_room.get("presence_sensor_adress");
			    

			    String temperature_actuator_address = (String) addr_room.get("temperature_actuator_address");
			    

			    String light_actuator_address = (String) addr_room.get("light_actuator_address");

			    coapList.put("A", new COAPActuatorSensorAddress(room_name,presence_sensor_adress,temperature_actuator_address,light_actuator_address));
			  }
			
		}
        catch(Exception e)
        {e.printStackTrace();
        System.exit(2);}
	}
	public COAPActuatorSensorAddress getCOAPActuatorSensorAddressForRoom(String room)
	{
		if(coapList.containsKey(room))
		{
			return coapList.get(room);
		}
		else
		{
			return null;
		}
	}
	public Set<String> getSetKey()
	{
		return coapList.keySet();
	}

}
