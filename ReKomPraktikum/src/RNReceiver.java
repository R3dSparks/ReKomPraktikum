import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import rn.*;

public class RNReceiver implements Receiver{

	private NetworkCard nc;
	
	public short Address = 4321;
	
	private int lastFrameAcknowledged = -1;
	
	private int windowSize = 3;
	
	private String m_savePath = "data.out";
	
	private FrameBuffer m_buffer;
	
	public static void main(String[] args) {
		try {
			RNReceiver receiver = new RNReceiver();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public RNReceiver() throws IOException {
		nc = new NetworkCard(this);
		m_buffer = new FrameBuffer(windowSize);
	}
	
	public void send(Frame frame) {
		try {
			nc.send(frame.GetBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void receive(byte[] arg0) {
		
		Frame dataFrame = new Frame(arg0);
		
		if(dataFrame.CheckFrame() && dataFrame.DestinationAddress == this.Address) {
			
			if(dataFrame.SequenceNumber <= lastFrameAcknowledged + windowSize) {
								
				System.out.println(Helper.GetMilliTime() + ": Received frame " + dataFrame.SequenceNumber);
				
				
				Frame ackFrame = new Frame(Address, dataFrame.SourceAddress, dataFrame.SequenceNumber > lastFrameAcknowledged ? dataFrame.SequenceNumber : lastFrameAcknowledged, new byte[0], true, dataFrame.Terminating);
				
				this.send(ackFrame);
				
				
				if(dataFrame.SequenceNumber <= lastFrameAcknowledged) {
					System.out.println(Helper.GetMilliTime() + ": Resending acknowledge for " + ackFrame.SequenceNumber + " to " + ackFrame.DestinationAddress);					
				}else {
					m_buffer.AddFrame(dataFrame);
					System.out.println(Helper.GetMilliTime() + ": Send acknowledge for " + ackFrame.SequenceNumber + " to " + ackFrame.DestinationAddress);					
				}
				
				Frame saveFrame = m_buffer.GetNextFrame();
				
				while(saveFrame != null) {
					lastFrameAcknowledged = saveFrame.SequenceNumber;
					writeLine(saveFrame.Payload);
					saveFrame = m_buffer.GetNextFrame();
				}
				
			}			
			
		}	
		else {
			System.out.println(Helper.GetMilliTime() + ": Received invalid frame!");
		}
		
	}
	
	private void writeLine(byte[] data) {
				
		String line = byteArrayToHexString(data);
		
		try {
			FileWriter fw = new FileWriter(m_savePath, true);
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write(line);
			
			bw.newLine();
			
			bw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String byteArrayToHexString(byte[] data) {
		String line = "";
		
		for(int i = 0; i < data.length; i++) {
			line += String.format("%02x", data[i]);
			line += " ";
		}
		
		return line;
	}

}
