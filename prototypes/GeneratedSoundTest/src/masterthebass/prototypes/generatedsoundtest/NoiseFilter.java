package masterthebass.prototypes.generatedsoundtest;
import java.util.Random;


public class NoiseFilter extends Filter  {

	public NoiseFilter(int ID, String name) {
		super(ID, name);
		// TODO Auto-generated constructor stub
	}
	


	@Override
	byte[] applyFilter (byte[] rawPCM){
	    Random generator = new Random();
		int count = rawPCM.length;
		 for (int i=0;i<count;i++)
		 {
			 int randomIndex = (generator.nextInt(20) - 10);
		     rawPCM[i] =(byte)(rawPCM[i] + randomIndex);
		   }

		return rawPCM;
	}
}
