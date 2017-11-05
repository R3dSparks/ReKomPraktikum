import java.io.IOException;

public class FrameSender extends Thread{
	
	public boolean Acknowledged = false;

	private Frame m_frame;
	
	private int m_timeOut;
	
	private RNSender m_sender;
	
	public FrameSender(Frame frame, RNSender sender, int timeOut) {
		this.m_frame = frame;
		this.m_sender = sender;
		this.m_timeOut = timeOut;
	}
	
	@Override
	public void run() {
		try {
			send();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
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
			m_sender.SenderNetworkCard.send(m_frame.GetBytes());
			
			System.out.println(Helper.GetMilliTime() + ": Send frame " + m_frame.SequenceNumber + " to address " + m_frame.DestinationAddress);
			
			Thread.sleep(m_timeOut);
			
			if(Acknowledged == false) {
				System.out.println(Helper.GetMilliTime() + ": Frame " + m_frame.SequenceNumber + " timed out!");
			}
			
		}

	}
	
}
