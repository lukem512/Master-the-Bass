package com.masterthebass;

public class Filter {
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
