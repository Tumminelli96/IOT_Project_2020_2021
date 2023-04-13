package iot.unipi.it;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class Collector {
	
	public static void main(String[] args) {
		COAPActuatorSensorAddressList coapActuatorSensorAddressList = new COAPActuatorSensorAddressList("../iot.unipi.it/src/main/resources/address.json");
		ConcurrentHashMap<String,ParameterRoom> room_parameter= new ConcurrentHashMap<String,ParameterRoom>();
		HashMap<String,String>actuator_temperature_value= new HashMap<String,String>();
		HashMap<String,String>actuator_light_value = new HashMap<String,String>();
		TopicObserver temp_obs;
		TopicObserver light_obs;
		boolean exit_condition=false;
        String topic1        = "actuator_temperature_";
        String content1      = "0";   
        String topic2        = "actuator_light_";
        String content2      = "0";
        String broker       = "tcp://[fd00::1]:1883";
        String clientId     = UUID.randomUUID().toString();
        Scanner scanner = new Scanner(System.in);
       System.out.println("Testbed broker?Type Y or YES if the broker is in the testbed if not enter any other word");
       String command=scanner.nextLine();
       command=command.toUpperCase();
       scanner.close();
       
       if(command.compareTo("Y")==0 || command.compareTo("YES")==0)
       {
    	   System.out.println("testbed");
    	   broker="tcp://127.0.0.1:1883";
       }
       else
       {
    	   System.out.println("cooja");
    	   broker="tcp://[fd00::1]:1883";
       }
        try {
        	CommunicationWithClient communication_with_client= new CommunicationWithClient(room_parameter);
        	Thread tread_communication = new Thread(communication_with_client);
        	tread_communication.start();
        	temp_obs=new TopicObserver("temperature_status",broker,UUID.randomUUID().toString());
    		light_obs=new TopicObserver("light_status",broker,UUID.randomUUID().toString());
    		CoapClientHandler coap_client = new CoapClientHandler();
    		Set<String> key_room_list = coapActuatorSensorAddressList.getSetKey();
    		//Start Observing presence resource of a specific room
    		for(String room:key_room_list)
    		{
    			coap_client.StartObserving(room, "presence", coapActuatorSensorAddressList.getCOAPActuatorSensorAddressForRoom(room).getPresence_sensor_adress());
    			coap_client.StartObserving(room, "light", coapActuatorSensorAddressList.getCOAPActuatorSensorAddressForRoom(room).getLight_actuator_address());
    		}
    		
    		//Start the communication with the MqttBroker
    		MqttClient sampleClient = new MqttClient(broker, clientId);
    		 System.out.println("Connecting to broker: "+broker);
             sampleClient.connect();
            
        	while(true)
        	{
        		
        		Set<String> rooms=temp_obs.getListOfAvaiableRoom();
        		for(String room:rooms)
        		{
        			if(!temp_obs.roomExist(room)) 
        			{
        				System.out.println("room not exist yet");
        				continue;
        			}
        			
        			
        			if(!temp_obs.there_is_new_value(room))//if no new sample is reported from MQTT node for the current room the room temperature check 
        												 //will be skipped 
                    {
                    	continue;
                    }
        			//initialize room parameters with default value if they are not already present
        			if(!room_parameter.containsKey(room))
        			{
        				room_parameter.put(room, new ParameterRoom());
        			}
        			if(!actuator_temperature_value.containsKey(room)) 
        			{
        				actuator_temperature_value.put(room, "0");
        			}
        			double temp_observ=temp_obs.getLastValueSensed(room);
        			//Send an actuator command
        			if(room_parameter.get(room).isAutomatic_temperature_actuator())
        			{
        				//case automatic temperature is activated
        				
                    	if(temp_observ<room_parameter.get(room).getTemp_inf_limit())
        				{
                    		
                    		content1="1";//enable warm system
                    		if(content1.compareTo(actuator_temperature_value.get(room))==0)
                    		{
                    			continue;
                    		}
                    		System.out.println("Publishing message: "+content1);
                            MqttMessage message = new MqttMessage(content1.getBytes());
                            sampleClient.publish(topic1+room, message);
                            System.out.println("Message published");
                            actuator_temperature_value.put(room, content1);
                            if(coapActuatorSensorAddressList.getCOAPActuatorSensorAddressForRoom(room)!=null)
                            {
                            	coap_client.sendActuatorCommand(room, "temperature_actuator",
                                		coapActuatorSensorAddressList.getCOAPActuatorSensorAddressForRoom(room).getTemperature_actuator_address(), 
                                		"on", Integer.parseInt(content1));
                            }
                            
                            
        				}
                    	else if(temp_observ>room_parameter.get(room).getTemp_sup_limit())
                    	{
                    		content1="2";//enable cool system
                    		if(content1.compareTo(actuator_temperature_value.get(room))==0)
                    		{
                    			continue;
                    		}
                    		System.out.println("Publishing message: "+content1);
                            MqttMessage message = new MqttMessage(content1.getBytes());
                            sampleClient.publish(topic1+room, message);
                            System.out.println("Message published");
                            actuator_temperature_value.put(room, content1);
                            if(coapActuatorSensorAddressList.getCOAPActuatorSensorAddressForRoom(room)!=null)
                            {
                            	coap_client.sendActuatorCommand(room, "temperature_actuator",
                                		coapActuatorSensorAddressList.getCOAPActuatorSensorAddressForRoom(room).getTemperature_actuator_address(),
                                		"on", Integer.parseInt(content1));
                            }
                            
                    	}
                    	else
                    	{
                    		content1="0";//actuator disabled
                    		if(content1.compareTo(actuator_temperature_value.get(room))==0)
                    		{
                    			continue;
                    		}
                    		System.out.println("Publishing message: "+content1);
                            MqttMessage message = new MqttMessage(content1.getBytes());
                            sampleClient.publish(topic1+room, message);
                            System.out.println("Message published");
                            actuator_temperature_value.put(room, content1);
                            if(coapActuatorSensorAddressList.getCOAPActuatorSensorAddressForRoom(room)!=null) 
                            {
                            	coap_client.sendActuatorCommand(room, "temperature_actuator",
                                		coapActuatorSensorAddressList.getCOAPActuatorSensorAddressForRoom(room).getTemperature_actuator_address(),
                                		"off", Integer.parseInt(content1));
                            }
                            
                    	}
        			}
        			else 
        			{ //case if the automatic control temperature of the room is disabled 
        				content1=Integer.toString(room_parameter.get(room).getTemperature_actuator_manual_mode());
                		if(content1.compareTo(actuator_temperature_value.get(room))==0)
                		{
                			continue;
                		}
                		System.out.println("Publishing message: "+content1);
                        MqttMessage message = new MqttMessage(content1.getBytes());
                        sampleClient.publish(topic1+room, message);
                        System.out.println("Message published");
                        actuator_temperature_value.put(room, content1);
                        if(coapActuatorSensorAddressList.getCOAPActuatorSensorAddressForRoom(room)!=null) 
                        {
                        	String mode= "on";
                        	if(room_parameter.get(room).getTemperature_actuator_manual_mode()==0)
                        	{
                        		mode="off";
                        	}
                        	coap_client.sendActuatorCommand(room, "temperature_actuator",
                            		coapActuatorSensorAddressList.getCOAPActuatorSensorAddressForRoom(room).getTemperature_actuator_address(),
                            		mode, Integer.parseInt(content1));
                        }
        			}
        			
        		}
        		for(String room:light_obs.getListOfAvaiableRoom())
        		{
        			
        			//initialize room parameters with default value if they are not already present
        			if(!room_parameter.containsKey(room))
        			{
        				room_parameter.put(room, new ParameterRoom());
        			}
        			if(!actuator_light_value.containsKey(room)) 
        			{
        				actuator_light_value.put(room, "0");
        			}
        			if(!light_obs.roomExist(room)) 
        			{
        				System.out.println("room not exist yet");
        				continue;
        			}
        			
                    if(!light_obs.there_is_new_value(room)&&!coap_client.is_new_value(room, "presence")&&room_parameter.get(room).isAutomatic_light_mode())
                    {
                    	continue;
                    }
                  //Send an actuator command
                	double new_light_observ=0.0;
                	if(room_parameter.get(room).isAutomatic_light_mode())//read a value from MQTTClient if automatic light mode is active
                	{
                		new_light_observ=light_obs.getLastValueSensed(room);
                	}
                	
                    if(room_parameter.get(room).isAutomatic_light_mode())
                    {
                    	
                    
	                	if(new_light_observ<room_parameter.get(room).getLight_min_limit()&&(coap_client.ObtainValue(room,"presence")==1))
	                	{
	                		
	                		content2="1";//light system enabled
	                		if(content2.compareTo(actuator_light_value.get(room))!=0)
	                		{
	                			System.out.println("Publishing message light: "+content2);
		                        MqttMessage message = new MqttMessage(content2.getBytes());
		                        sampleClient.publish(topic2+room, message);
		                        System.out.println("Message published");
		                        actuator_light_value.put(room, content2);
		                        coap_client.sendActuatorCommand(room, "light",
	                            		coapActuatorSensorAddressList.getCOAPActuatorSensorAddressForRoom(room).getLight_actuator_address(),
	                            		"on", Integer.parseInt(content2));
	                		}
	                		
	                	}
	                	else
	                	{
	                		//switch off the light in case no one in the room or the brightness of the room is too high
	                		if(coap_client.ObtainValue(room,"presence")==0||
	                				(content2.compareTo("1")==0 &&
	                				(new_light_observ-room_parameter.get(room).getLight_intensity())>room_parameter.get(room).getLight_min_limit()))
	                		{
	                			content2="0";//actuator disabled
	                			if(content2.compareTo(actuator_light_value.get(room))!=0)
	                			{
	                				System.out.println("Publishing message: "+content2);
		                            MqttMessage message = new MqttMessage(content2.getBytes());
		                            sampleClient.publish(topic2+room, message);
		                            System.out.println("Message published");
		                            actuator_light_value.put(room, content2);
		                            coap_client.sendActuatorCommand(room, "light",
		                            		coapActuatorSensorAddressList.getCOAPActuatorSensorAddressForRoom(room).getLight_actuator_address(), 
		                            		"off", Integer.parseInt(content2));
	                			}
	                    		
	                		}
	                		
	                	}
                    }
                    if(coap_client.is_new_value(room, "light"))
                    {
                    	content2=Integer.toString(coap_client.ObtainValue(room, "light"));//actuator disabled
                		System.out.println("Publishing message: "+content2);
                        MqttMessage message = new MqttMessage(content2.getBytes());
                        sampleClient.publish(topic2+room, message);
                        System.out.println("Message published");
                        actuator_light_value.put(room, content2);
                    }
                
        		}     
        		if(exit_condition) 
        		{
        			sampleClient.disconnect();
                    System.out.println("Disconnected");
                    System.exit(0);
        		}
        	}
        	
        	

        } catch(MqttException me) {

            me.printStackTrace();
            //sampleClient.disconnect();
            System.out.println("Disconnected");
            System.exit(2);
        }
        catch(Exception e)
        {e.printStackTrace();
        System.exit(2);}
    }

}
