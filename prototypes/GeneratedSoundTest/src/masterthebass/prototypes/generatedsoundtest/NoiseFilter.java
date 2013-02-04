package masterthebass.prototypes.generatedsoundtest;
import java.util.Random;


public class NoiseFilter extends Filter  {
	int range;
	
	public NoiseFilter(int ID, String name) {
		super(ID, name);
		//set default range
		range = 20;
	}
	


	public void setRange(int newrange){
		this.range = newrange;
	}
	
	public int getRange(){
		return this.range;
	}
		
	@Override
	byte[] applyFilter (byte[] rawPCM){
	    Random generator = new Random();
		int count = rawPCM.length;
		for (int i=0;i<count;i++){
			int randomIndex = (generator.nextInt(range) - (range/2));
		    rawPCM[i] =(byte)(rawPCM[i] + randomIndex);
		 }

		return rawPCM;
	}
}
