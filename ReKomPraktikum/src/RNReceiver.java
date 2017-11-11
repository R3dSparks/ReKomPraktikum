import java.io.FileOutputStream;
import java.io.IOException;

import rn.*;

public class RNReceiver implements Receiver{

	private NetworkCard nc;
	
	public short Adress;
	
	private short m_sourceAdress;
	
	private TimedTerminator m_terminator;
	
	private boolean m_terminating = false;
	
	private int lastFrameAcknowledged = -1;
	
	private int m_windowSize;
	
	private String m_savePath = "data.out";
	
	private FrameBuffer m_buffer;
	
	public static void main(String[] args) {
		short sourceAdress = Short.parseShort(args[0]);
		
		short destinationAdress = Short.parseShort(args[1]);
		
		int windowSize = Integer.parseInt(args[2]);
		
		try {
			RNReceiver receiver = new RNReceiver(sourceAdress, destinationAdress, windowSize);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public RNReceiver(short sourceAdress, short destinationAdress, int windowSize) throws IOException {
		this.Adress = destinationAdress;
		m_sourceAdress = sourceAdress;
		m_windowSize = windowSize;
		
		nc = new NetworkCard(this);
		m_buffer = new FrameBuffer(m_windowSize);
	}
	
	public void send(Frame frame) {
		try {
			nc.send(frame.GetBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void receive(byte[] arg0) {
		
		if(m_terminator != null) {
			m_terminator.interrupt();
			m_terminator = null;
		}
		
		Frame dataFrame = new Frame(arg0);
		
		if(dataFrame.CheckFrame() && dataFrame.getDestinationAddress() == this.Adress && dataFrame.getSequenceNumber() == m_sourceAdress) {
			
			if(dataFrame.isTerminating()) {
				m_terminating = true;
			}
						
			// Resending acknowledge
			if(dataFrame.getSequenceNumber() <= lastFrameAcknowledged) {
				
				Frame ackFrame = new Frame(Adress, dataFrame.getSequenceNumber(), lastFrameAcknowledged, new byte[0], true, dataFrame.isTerminating());
				
				System.out.println(Helper.GetMilliTime() + ": Resending acknowledge for " + ackFrame.getSequenceNumber() + " to " + ackFrame.getDestinationAddress());					
				
				this.send(ackFrame);
					
			} else			
			// Sending acknowledge for new frame within window size
			if(dataFrame.getSequenceNumber() <= lastFrameAcknowledged + m_windowSize) {
				
				System.out.println(Helper.GetMilliTime() + ": Received frame " + dataFrame.getSequenceNumber());

				m_buffer.AddFrame(dataFrame);
				
				Frame writeFrame = m_buffer.GetNextFrame();
				
				while(writeFrame != null) {
					writeLine(writeFrame.getPayload());
					lastFrameAcknowledged++;
					writeFrame = m_buffer.GetNextFrame();
				}
				
				Frame ackFrame = new Frame(Adress, dataFrame.getSequenceNumber(), lastFrameAcknowledged, new byte[0], true, dataFrame.isTerminating());
				
				this.send(ackFrame);
				
				System.out.println(Helper.GetMilliTime() + ": Sending acknowledge for " + ackFrame.getSequenceNumber() + " to " + ackFrame.getDestinationAddress());								
			}
			
			if(m_terminating) {
				m_terminator = new TimedTerminator(1000);
				m_terminator.start();
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
			FileOutputStream fos = new FileOutputStream(m_savePath, true);
			
			fos.write(data);
			
			fos.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
