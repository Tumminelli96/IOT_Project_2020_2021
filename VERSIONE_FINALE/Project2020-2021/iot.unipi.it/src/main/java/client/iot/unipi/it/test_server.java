package client.iot.unipi.it;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import iot.unipi.it.CommunicationWithClient;
import iot.unipi.it.ParameterRoom;

public class test_server {
	
	public static void main(String[] args) {
		ConcurrentHashMap<String,ParameterRoom> room_parameter= new ConcurrentHashMap<String,ParameterRoom>();
		CommunicationWithClient communication_with_client= new CommunicationWithClient(room_parameter);
    	Thread tread_communication = new Thread(communication_with_client);
    	tread_communication.start();
    	room_parameter.put("A", new ParameterRoom());
    	while(true) 
    	{
    		System.out.println(room_parameter.get("A"));
    		try {
				TimeUnit.SECONDS.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
	}
}
