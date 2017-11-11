import java.io.FileOutputStream;
import java.io.IOException;

import rn.NetworkCard;

public class RNReceiver {

	public static void main(String[] args) {
		
		
		short sourceAdress = Short.parseShort(args[0]);
		short destinationAdress = Short.parseShort(args[1]);
		int windowSize = Integer.parseInt(args[2]);
		
		try {
			FrameReceiver receiver = new FrameReceiver(sourceAdress, destinationAdress, windowSize);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
