import java.io.IOException;
import java.util.ArrayList;

import rn.*;

public class RNSender implements Receiver{

	public NetworkCard SenderNetworkCard;
	
	public short Address = 1234;
	
	private int windowSize = 3;
	
	private int lastFrameSend = 0;
	
	private int lastFrameAck = 0;
	
	private int timeOut = 200;
	
	private ArrayList<FrameSender> buffer;
	
	
	public static void main(String[] args) {
		
		RNSender sender;
		
		short destination = 4321;
		
		TestData td = TestData.createTestData(0);
		
		try {
			sender = new RNSender();
			
			sender.send(td, 10, destination);			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	
	public RNSender() throws IOException {
		SenderNetworkCard = new NetworkCard(this);
		buffer = new ArrayList<FrameSender>();
	}
	
	
	public void send(TestData td, int numberOfFrames, short destinationAddress) throws IOException, InterruptedException {
		
		while(lastFrameSend < numberOfFrames) {

			synchronized(this) {
				while(lastFrameSend - lastFrameAck < windowSize && lastFrameSend < numberOfFrames) {
					Frame frame = new Frame(this.Address, destinationAddress, lastFrameSend, td.getTestData(), false, lastFrameSend == numberOfFrames - 1 ? true : false);
					FrameSender fs = new FrameSender(frame, this, timeOut);
				
					lastFrameSend++;
					
					buffer.add(fs);
					
					fs.start();
				}
			
				wait();
			}
		}
		
	}
	
	@Override
	public void receive(byte[] arg0) {
		Frame ackFrame = new Frame(arg0);
		
		synchronized(this) {
		
			if(ackFrame.CheckFrame() && ackFrame.DestinationAddress == this.Address && ackFrame.Acknowledge) {
				if(ackFrame.Terminating) {
					System.out.println(Helper.GetMilliTime() + ": Received acknowledge for terminating frame " + ackFrame.SequenceNumber);
					System.out.println("Terminating");
					System.exit(0);
				}
				
				System.out.println(Helper.GetMilliTime() + ": Received acknowledge for frame " + ackFrame.SequenceNumber);
				lastFrameAck = ackFrame.SequenceNumber;
			
				for(int i = 0; i < buffer.size(); i++) {
					if(buffer.get(i).GetFrame().SequenceNumber <= lastFrameAck) {
						buffer.get(i).Acknowledged = true;
						buffer.remove(i);
					}
				}
				
			}
			else {
				System.out.println(Helper.GetMilliTime() + ": Received invalid frame");
			}	
			
			notify();
		}
	}	

}
