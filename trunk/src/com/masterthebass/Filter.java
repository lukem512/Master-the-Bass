package com.masterthebass;

import java.io.Serializable;

public class Filter implements Serializable {
	private static final long serialVersionUID = -176105425284552882L;
	
	private int ID;
	private String name;
	private boolean enabled;
	
	public Filter(int ID, String name){
		this.name = name;
		this.ID = ID;
	}
	
	public boolean getState (){
		return enabled;
	}
	
	public String getName (){
		return name;
	}
	
	public int getID (){
		return ID;
	}
	
	public byte[] applyFilter (byte[] rawPCM){
		return rawPCM;
	}
	
	public short[] applyFilter (short[] rawPCM){
		return rawPCM;
	}
	
	public void enable (){
		enabled = true;
	}
	
	public void disable (){
		enabled = false;
	}
		
	public void toggle (){
		//toggle between enabled and disabled
		enabled = !enabled;
	}
}