import java.io.IOException;

import rn.NetworkCard;

public class FrameSender extends Thread{
	
	private boolean acknowledged = false;
	private Frame frame;
	private int timeOut;
	private NetworkCard networkCard;


	
	public FrameSender(Frame frame, NetworkCard nc, int timeOut) {
		this.frame = frame;
		this.networkCard = nc;
		this.timeOut = timeOut;
	}
	
	@Override
	public void run() {
		try {
			send();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void send() throws IOException, InterruptedException {
		while(this.acknowledged == false) {
			this.networkCard.send(this.frame.GetBytes());
			
			System.out.println(Helper.GetMilliTime() + ": Send frame " + this.frame.getSequenceNumber() + " to address " + this.frame.getDestinationAddress());
			
			Thread.sleep(this.timeOut);
			
			if(this.acknowledged == false) {
				System.out.println(Helper.GetMilliTime() + ": Frame " + this.frame.getSequenceNumber() + " timed out!");
			}
			
		}

	}
	
	//
	// Getter and Setter
	//

	public boolean isAcknowledged() {
		return this.acknowledged;
	}

	public void setAcknowledged(boolean acknowledged) {
		this.acknowledged = acknowledged;
	}
	
	public int getSequenceNumber() {
		return this.frame.getSequenceNumber();
	}
	
	public Frame getFrame() {
		return this.frame;
	}
	
}
