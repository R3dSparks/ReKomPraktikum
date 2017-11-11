import java.io.IOException;
import java.util.ArrayList;

import rn.*;

public class RNSender implements Receiver{

	private NetworkCard m_networkCard;
	
	private short m_adress;
	
	private short m_destinationAdress;
	
	private int m_windowSize;
	
	private int lastFrameSend = 0;
	
	private int lastFrameAck = 0;
	
	private int timeOut = 200;
	
	private ArrayList<FrameSender> buffer;
	

	public static void main(String[] args) {
		
		short sourceAdress = Short.parseShort(args[0]);
		
		short destinationAdress = Short.parseShort(args[1]);
		
		int windowSize = Integer.parseInt(args[2]);
		
		int testData = Integer.parseInt(args[3]);
		
		TestData td = TestData.createTestData(testData);
		
		try {
			RNSender sender = new RNSender(sourceAdress, destinationAdress, windowSize);
			
			td.writeToFile("data.in");
			
			sender.send(td);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	
	public RNSender(short sourceAdress, short destinationAdress, int windowSize) throws IOException {
		this.m_adress = sourceAdress;
		this.m_destinationAdress = destinationAdress;
		this.m_windowSize = windowSize;
		
		m_networkCard = new NetworkCard(this);
		buffer = new ArrayList<FrameSender>();
	}
	
	
	public void send(TestData td) throws IOException, InterruptedException {
		
		byte[] data = td.getTestData();
		
		synchronized(this) {
			while(data != null) {
				
				while(lastFrameSend < lastFrameAck + m_windowSize && data != null) {
					Frame frame = new Frame(this.m_adress, this.m_destinationAdress, lastFrameSend, data, false, false);
					FrameSender fs = new FrameSender(frame, this.m_networkCard, timeOut);
					
					data = td.getTestData();
					
					if(data == null) {
						frame.setTerminating(true);
					}
					
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
		
			if(ackFrame.CheckFrame() && ackFrame.getDestinationAddress() == this.m_adress && ackFrame.getSequenceNumber() == this.m_destinationAdress && ackFrame.isAcknowledge()) {
				
				// Terminate if the terminating acknowladge is received
				if(ackFrame.isTerminating()) {
					System.out.println(Helper.GetMilliTime() + ": Received acknowledge for terminating frame " + ackFrame.getSequenceNumber());
					System.out.println("Terminating");
					System.exit(0);
				}
				
				System.out.println(Helper.GetMilliTime() + ": Received acknowledge for frame " + ackFrame.getSequenceNumber());
				lastFrameAck = ackFrame.getSequenceNumber();
			
				for(int i = 0; i < buffer.size(); i++) {
					if(buffer.get(i).GetFrame().getSequenceNumber() <= lastFrameAck) {
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
