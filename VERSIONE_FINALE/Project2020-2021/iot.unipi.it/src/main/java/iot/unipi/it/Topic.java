package iot.unipi.it;

import java.util.ArrayList;

public class Topic {
	private String name;
	private ArrayList<Double> listOfTopicValueSensed;
	public Topic(String name)
	{
		this.name=name;
		this.listOfTopicValueSensed=new ArrayList<Double>();
	}
	public String getName()
	{
		return name;
	}
	public double extractValue()
	{
		double result=listOfTopicValueSensed.get(0);
		listOfTopicValueSensed.remove(0);
		return result;
	}
	public void setName(String name)
	{
		this.name=name;
	}
	public void addValue(double value)
	{
		listOfTopicValueSensed.add(value);
	}
}
