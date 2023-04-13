package iot.unipi.it;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
public class TopicObserver implements MqttCallback{
	
	private String topic;
	private String broker;
    private String clientId;
    
    private ConcurrentHashMap<String,Boolean> room_has_new_value;
    private ConcurrentHashMap<String,ArrayList<Double>> room_value_sensed;//da implementare tutto il meccanismo per mantenere coppie stanza temperatura
	public TopicObserver() throws MqttException {
        topic        = "light_status";
        broker       = "tcp://[fd00::1]:1883";
        //broker       = "tcp://127.0.0.1:1883";
        clientId     = "JavaMQTTApp";
		room_value_sensed=new ConcurrentHashMap<String,ArrayList<Double>>();
		room_has_new_value=new ConcurrentHashMap<String,Boolean>();
		MqttClient mqttClient = new MqttClient(broker, clientId);
        System.out.println("Connecting to broker: "+broker);
        
        mqttClient.setCallback( this );
        
        mqttClient.connect();
        
        mqttClient.subscribe(topic);
	}
	public TopicObserver(String topic,String broker,String clientId) throws MqttException {
        this.topic        = topic;
        this.broker       = "tcp://127.0.0.1:1883";
        this.clientId     = "JavaMQTTApp";
        room_value_sensed=new ConcurrentHashMap<String,ArrayList<Double>>();
        room_has_new_value=new ConcurrentHashMap<String,Boolean>();
		MqttClient mqttClient = new MqttClient(broker, clientId);
        System.out.println("Connecting to broker: "+broker);
        
        mqttClient.setCallback( this );
        
        mqttClient.connect();
        
        mqttClient.subscribe(topic);
	}
	public Set<String> getListOfAvaiableRoom()
	{
		return room_value_sensed.keySet();
	}
	public boolean roomExist(String room) 
	{
		return room_value_sensed.containsKey(room);
	}
	//Function return the last value sensed from a MQTT sensor
	public double getLastValueSensed(String room) 
	{
		if(!room_value_sensed.containsKey(room)) 
		{
			throw new ArithmeticException("The room doesn't exist");
			
		}
		if(room_value_sensed.get(room).size()==1) 
		{
			room_has_new_value.put(room, false);
			return room_value_sensed.get(room).get(0);
		}
		double result=room_value_sensed.get(room).get(0);
		room_value_sensed.get(room).remove(0);
		return result;
	}
	
    public void add_value_at_room(String topic,double value)
    {
    	if(room_value_sensed.containsKey(topic))
    	{
    		if(room_value_sensed.get(topic).size()==1 && !room_has_new_value.get(topic))
    		{
    			room_value_sensed.get(topic).remove(0);//if the value on the corresponding array has only one value and is already processed, it will be deleted before the insertion of a new value
    		}
    		room_value_sensed.get(topic).add(value);
    		room_has_new_value.put(topic, true);
    	}
    	else 
    	{
    		room_value_sensed.put(topic,new ArrayList<Double>());
    		room_value_sensed.get(topic).add(value);
    		room_has_new_value.put(topic, true);
    	}
    	
    }
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub
	}

	public void messageArrived(String topic, MqttMessage message) throws Exception {
		JSONParser parser = new JSONParser();
		System.out.println("Get a message"+topic);
        System.out.println(String.format("[%s] %s", topic, new String(message.getPayload())));
        
        //Format message JSON {'room':"a",'value':14.819164,'unit_of_measure':"C"}
        
        JSONObject json = (JSONObject) parser.parse(new String(message.getPayload()));
        add_value_at_room((String)json.get("room"),(Double)json.get("value"));
        LocalDateTime time= LocalDateTime.now();
        System.out.println(String.format("Type of measurement=%s; Room=%s;Value=%f %s,Time collected value=%s",topic
        		,(String)json.get("room"),(Double)json.get("value"),(String)json.get("unit_of_measure"),time));
        MysqlAccess.insert_statment(topic, (String)json.get("room"), (Double)json.get("value"), (String)json.get("unit_of_measure"), Timestamp.valueOf(time));
        
	}
	public boolean there_is_new_value(String room)
	{
		
		if(!room_has_new_value.containsKey(room))
		{
			room_has_new_value.put(room, false);
		}
		return room_has_new_value.get(room);
	}
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
	}
	
}
