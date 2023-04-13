package iot.unipi.it;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.Request;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CoapClientHandler {
	private ConcurrentHashMap<String,CoapClient> coapClientList;//id for each CoapClient: roomName_deviceType
	private ConcurrentHashMap<String,CoapObserveRelation> coapObservingRelation;
	private ConcurrentHashMap<String,ArrayList<Integer>> listOfValue;
	private ConcurrentHashMap<String,Boolean> room_has_new_value;
	
	public CoapClientHandler() 
	{
		room_has_new_value=new ConcurrentHashMap<String,Boolean>();
		coapClientList=new ConcurrentHashMap<String,CoapClient>();
		coapObservingRelation=new ConcurrentHashMap<String,CoapObserveRelation>();
		listOfValue=new ConcurrentHashMap<String,ArrayList<Integer>>();
		
	}
	// function used to observe a specific COAP resource
	public void StartObserving(String room,String device,String address) 
	{
		final String IdString=room+"_"+device;
		final String dev=device;
		final String rooms=room;
		if(!coapClientList.containsKey(room+"_"+device)) 
		{
			coapClientList.put(room+"_"+device, new CoapClient("coap://"+address+"/"+device));
			listOfValue.put(room+"_"+device,new ArrayList<Integer>() );
			
		}
		if(!coapObservingRelation.containsKey(room+"_"+device))
		{
			CoapObserveRelation relation = coapClientList.get(room+"_"+device).observe(new CoapHandler() {
	           
	            public void onLoad(CoapResponse response) {
	            	JSONParser parser = new JSONParser();
	            	try {
						JSONObject json = (JSONObject) parser.parse(new String(response.getResponseText()));
						
						
						if(!listOfValue.get(IdString).isEmpty()&&!room_has_new_value.get(IdString))
						{
							listOfValue.get(IdString).remove(0);
						}
						listOfValue.get(IdString).add(((Long)json.get("value")).intValue());
						room_has_new_value.put(IdString,true);
						LocalDateTime time = LocalDateTime.now();
						System.out.println(String.format("Type of measurement=%s; Room=%s;Value=%d,Time collected value=%s",dev
				        		,rooms,((Long)json.get("value")).intValue(),time));
						if(dev.compareTo("light")==0)
						{
							MysqlAccess.insert_statment("switch_light", rooms, (Long)json.get("value"), "", Timestamp.valueOf(time));//save switch_state on db
						}
						else
						{
							MysqlAccess.insert_statment(dev, rooms, (Long)json.get("value"), "", Timestamp.valueOf(time));//save presence sensor  value on db
						}
						
						
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						System.out.println("Device:"+ dev);
						e.printStackTrace();
					}
	            	
	            	
	            	
	            }	            
	            public void onError() {
	                System.out.println("Failed");
	            }
	        });
			coapObservingRelation.put(room+"_"+device,relation);
		}
	}
	public int ObtainValue(String room,String device) 
	{
		if(listOfValue.get(room+"_"+device).isEmpty())
		{
			return 0;
		}
		int result=listOfValue.get(room+"_"+device).get(0);
		if(listOfValue.get(room+"_"+device).size()>1)
		{
			listOfValue.get(room+"_"+device).remove(0);
		}
		else 
		{
			room_has_new_value.put(room+"_"+device,false);
		}
		return result;
	}
	//function used for know if a room has a new value
	public boolean is_new_value(String room,String device) 
	{
		if(!room_has_new_value.containsKey(room+"_"+device))
		{
			room_has_new_value.put(room+"_"+device,false);
		}
		return room_has_new_value.get(room+"_"+device);
	}
	//function used for command to COAP actuator
	public void sendActuatorCommand(String room,String device,String address,String command,int mode) 
	{
		String color;
		if(!coapClientList.containsKey(room+"_"+device)) 
		{
			coapClientList.put(room+"_"+device, new CoapClient("coap://"+address+"/"+device));
			
			
		}
		if(device.compareTo("temperature_actuator")==0)
		{
			if(mode==0)
			{
				color="r";
				command="off";
			}
			else if(mode==1)
			{
				color="r";
				command="on";
			}
			else if(mode==2)
			{
				color="g";
				command="on";
			}
			else
			{
				color="r";
				command="off";
			}
			Request req = new Request(Code.PUT);
			String payload="mode="+command;
			req.setPayload(payload);
			req.getOptions().addUriQuery ("color="+color);
			//req.getOptions().addaddUriQuery ("mode=\""+command+"\"");
			System.out.println(req);
			CoapResponse resp = coapClientList.get(room+"_"+device).advanced(req);
			System.out.println(resp);
		}
		else 
		{
			Request req = new Request(Code.PUT);
			String payload="mode="+command;
			req.setPayload(payload);
			CoapResponse resp = coapClientList.get(room+"_"+device).advanced(req);
			System.out.println(resp);
		}
	}

}
