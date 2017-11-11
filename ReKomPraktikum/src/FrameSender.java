import java.io.IOException;

import rn.NetworkCard;

public class FrameSender extends Thread{
	
	public boolean Acknowledged = false;

	private Frame m_frame;
	
	private int m_timeOut;
	
	private NetworkCard m_networkCard;
	
	public FrameSender(Frame frame, NetworkCard nc, int timeOut) {
		this.m_frame = frame;
		this.m_networkCard = nc;
		this.m_timeOut = timeOut;
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
	
	public int GetSequenceNumber() {
		return m_frame.SequenceNumber;
	}
	
	public Frame GetFrame() {
		return m_frame;
	}
	
	private void send() throws IOException, InterruptedException {
		while(Acknowledged == false) {
			m_networkCard.send(m_frame.GetBytes());
			
			System.out.println(Helper.GetMilliTime() + ": Send frame " + m_frame.SequenceNumber + " to address " + m_frame.DestinationAddress);
			
			Thread.sleep(m_timeOut);
			
			if(Acknowledged == false) {
				System.out.println(Helper.GetMilliTime() + ": Frame " + m_frame.SequenceNumber + " timed out!");
			}
			
		}

	}
	
}
