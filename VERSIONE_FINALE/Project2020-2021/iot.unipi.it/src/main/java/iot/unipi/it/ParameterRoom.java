package iot.unipi.it;

public class ParameterRoom {
	private double temp_sup_limit;
	private double temp_inf_limit;
	private double temp_ideal_limit;
	private double light_min_limit;
	private boolean automatic_light_mode;
	private boolean automatic_temperature_actuator;
	private double light_intensity;
	private int temperature_actuator_manual_mode;//0 off, 1 warming, 2 cooling
	public ParameterRoom() 
	{
		temp_sup_limit=22.0;
		temp_inf_limit=18.0;
		temp_ideal_limit=20.0;
		light_min_limit=1000.0;
		automatic_light_mode=true;
		automatic_temperature_actuator=true;
		temperature_actuator_manual_mode=0;
		light_intensity=1000;
	}
	public double getLight_intensity() 
	{
		return light_intensity;
	}
	public int getTemperature_actuator_manual_mode() {
		return temperature_actuator_manual_mode;
	}

	public void setTemperature_actuator_manual_mode(int temperature_actuator_manual_mode) {
		this.temperature_actuator_manual_mode = temperature_actuator_manual_mode;
	}

	public double getTemp_sup_limit() {
		return temp_sup_limit;
	}
	
	public void setTemp_sup_limit(double temp_sup_limit) {
		this.temp_sup_limit = temp_sup_limit;
	}
	public double getTemp_inf_limit() {
		return temp_inf_limit;
	}
	public void setTemp_inf_limit(double temp_inf_limit) {
		this.temp_inf_limit = temp_inf_limit;
	}
	public double getTemp_ideal_limit() {
		return temp_ideal_limit;
	}
	public void setTemp_ideal_limit(double temp_ideal_limit) {
		this.temp_ideal_limit = temp_ideal_limit;
	}
	public double getLight_min_limit() {
		return light_min_limit;
	}
	public void setLight_min_limit(double light_min_limit) {
		this.light_min_limit = light_min_limit;
	}
	public boolean isAutomatic_light_mode() {
		return automatic_light_mode;
	}
	public void setAutomatic_light_mode(boolean automatic_light_mode) {
		this.automatic_light_mode = automatic_light_mode;
	}
	public boolean isAutomatic_temperature_actuator() {
		return automatic_temperature_actuator;
	}
	public void setAutomatic_temperature_actuator(boolean temperature_actuator_on) {
		this.automatic_temperature_actuator = temperature_actuator_on;
	}
	public boolean setParameter(String parameter,String value)
	{
		switch(parameter)
		{
		case "temp_sup_limit":
			if(Double.parseDouble(value)<temp_inf_limit)
				return false;
			setTemp_sup_limit(Double.parseDouble(value));
			return true;
		case "temp_inf_limit":
			if(Double.parseDouble(value)>temp_sup_limit)
				return false;
			setTemp_inf_limit(Double.parseDouble(value));
			return true;
		case "temp_ideal_limit":
			setTemp_ideal_limit(Double.parseDouble(value));
			return true;
		case "light_min_limit":
			setLight_min_limit(Double.parseDouble(value));
			return true;
		case "automatic_light_mode":
			setAutomatic_light_mode(Boolean.parseBoolean(value));
			return true;
		case "automatic_temperature_actuator":
			setAutomatic_temperature_actuator(Boolean.parseBoolean(value));
			return true;
		default: return false;
		case "temperature_actuator_manual_mode":
			if(!isNumeric(value))
			{
				return false;
			}
			if(Integer.parseInt(value)>3)
			{
				return false;
			}
			setTemperature_actuator_manual_mode(Integer.parseInt(value));
			return true;
			
			
			
		}
	}

	private static boolean isNumeric(String str) { 
		  try {  
		    Integer.parseInt(str);  
		    return true;
		  } catch(NumberFormatException e){  
		    return false;  
		  }  
		}
	@Override
	public String toString() {
		return "ParameterRoom [temp_sup_limit=" + temp_sup_limit+ "\n temp_inf_limit=" + temp_inf_limit
				+ "\n temp_ideal_limit=" + temp_ideal_limit + "\n light_min_limit=" + light_min_limit
				+ "\n automatic_light_mode=" + automatic_light_mode + "\n automatic_temperature_actuator="
				+ automatic_temperature_actuator + "\n temperature_actuator_manual_mode="
				+ temperature_actuator_manual_mode + "]\n";
	}

}
