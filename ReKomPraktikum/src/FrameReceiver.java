import java.io.FileOutputStream;
import java.io.IOException;

import rn.NetworkCard;
import rn.Receiver;

public class FrameReceiver implements Receiver {
	
	private NetworkCard nc;
	private short adress;
	private short sourceAdress;
	private TimedTerminator terminator;
	private boolean terminating = false;
	private int lastFrameAcknowledged = -1;
	private int windowSize;
	private String savePath = "data.out";
	private FrameBuffer buffer;
	
	

	public FrameReceiver(short sourceAdress, short destinationAdress, int windowSize) throws IOException {
		this.adress = destinationAdress;
		this.sourceAdress = sourceAdress;
		this.windowSize = windowSize;
		
		this.nc = new NetworkCard(this);
		this.buffer = new FrameBuffer(this.windowSize);
	}
	
	
	
	public void send(Frame frame) {
		try {
			this.nc.send(frame.GetBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	@Override
	public void receive(byte[] arg0) {
		
		if(this.terminator != null) {
			this.terminator.interrupt();
			this.terminator = null;
		}
		
		Frame dataFrame = new Frame(arg0);
		
		if(dataFrame.CheckFrame() && dataFrame.getDestinationAddress() == this.adress && dataFrame.getSequenceNumber() == this.sourceAdress) {
			
			if(dataFrame.isTerminating()) {
				this.terminating = true;
			}
						
			// Resending acknowledge
			if(dataFrame.getSequenceNumber() <= this.lastFrameAcknowledged) {
				
				Frame ackFrame = new Frame(this.adress, dataFrame.getSequenceNumber(), this.lastFrameAcknowledged, new byte[0], true, dataFrame.isTerminating());
				
				System.out.println(Helper.GetMilliTime() + ": Resending acknowledge for " + ackFrame.getSequenceNumber() + " to " + ackFrame.getDestinationAddress());					
				
				this.send(ackFrame);
					
			} else			
			// Sending acknowledge for new frame within window size
			if(dataFrame.getSequenceNumber() <= this.lastFrameAcknowledged + this.windowSize) {
				
				System.out.println(Helper.GetMilliTime() + ": this.Received frame " + dataFrame.getSequenceNumber());

				this.buffer.AddFrame(dataFrame);
				
				Frame writeFrame = this.buffer.GetNextFrame();
				
				while(writeFrame != null) {
					writeLine(writeFrame.getPayload());
					this.lastFrameAcknowledged++;
					writeFrame = this.buffer.GetNextFrame();
				}
				
				Frame ackFrame = new Frame(this.adress, dataFrame.getSequenceNumber(), this.lastFrameAcknowledged, new byte[0], true, dataFrame.isTerminating());
				
				this.send(ackFrame);
				
				System.out.println(Helper.GetMilliTime() + ": Sending acknowledge for " + ackFrame.getSequenceNumber() + " to " + ackFrame.getDestinationAddress());								
			}
			
			if(this.terminating) {
				this.terminator = new TimedTerminator(1000);
				this.terminator.start();
			}
				
		} else {
			System.out.println(Helper.GetMilliTime() + ": Received invalid frame!");
			try {
				System.out.println("Sequence number: " + dataFrame.getCheckSum());
			} catch(Exception e) {
				System.out.println("Can't read invalid frame");
			}
		}
		
	}
	
	private void writeLine(byte[] data) {
		
		try {
			FileOutputStream fos = new FileOutputStream(this.savePath, true);
			
			fos.write(data);
			
			fos.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
}
