package iot.unipi.it;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
public class TemperatureObserver implements MqttCallback{
	private String topic        = "temperature_status";
    private String broker       = "tcp://127.0.0.1:1883";
    private String clientId     = "JavaMQTTApp";
    private double last_temperature_sensed;
    private boolean new_value;
	public TemperatureObserver() throws MqttException {
        topic        = "temperature_status";
        broker       = "tcp://127.0.0.1:1883";
        clientId     = "JavaMQTTApp";
		
		MqttClient mqttClient = new MqttClient(broker, clientId);
        System.out.println("Connecting to broker: "+broker);
        
        mqttClient.setCallback( this );
        
        mqttClient.connect();
        
        mqttClient.subscribe(topic);
        last_temperature_sensed=0.0;
        new_value=false;
	}
	public double getLastTemperatureSensed()
	{
		new_value=false;
		return last_temperature_sensed;
	}
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub
	}
	public boolean getNewValue()
	{
		return new_value;
	}
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		new_value=true;
        System.out.println(String.format("[%s] %s", topic, new String(message.getPayload())));
        last_temperature_sensed=Double.parseDouble(new String(message.getPayload()));
	}

	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
	}

}
