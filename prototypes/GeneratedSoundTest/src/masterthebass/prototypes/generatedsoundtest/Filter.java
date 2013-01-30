package masterthebass.prototypes.generatedsoundtest;

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
	
	
	String getName (){
		return name;
	}
	
	int getID (){
		return ID;
	}
	
	byte[] applyFilter (byte[] rawPCM){
		return rawPCM;
	}
	
	void enable (){
		enabled = true;
	}
	
	void disable (){
		enabled = false;
	}
		
	void toggle (){
		//toggle between enabled and disabled
		enabled = !enabled;
	}
}