import java.io.IOException;
import java.util.ArrayList;

import rn.NetworkCard;
import rn.Receiver;
import rn.TestData;

public class FrameSender implements Receiver {

	private NetworkCard networkCard;
	private short address;
	private short destinationAddress;
	private int windowSize;
	private int lastFrameSend = 0;
	private int lastFrameAck = 0;
	private int timeOut = 200;
	private ArrayList<FrameSenderThread> buffer;

	public FrameSender(short sourceAdress, short destinationAdress, int windowSize) throws IOException {
		this.address = sourceAdress;
		this.destinationAddress = destinationAdress;
		this.windowSize = windowSize;

		this.networkCard = new NetworkCard(this);
		this.buffer = new ArrayList<FrameSenderThread>();
	}

	public void send(TestData td) throws Exception {

		if (td == null)
			throw new IllegalStateException("TestData object is null. cannot run the send method.");

		byte[] data = td.getTestData();
		if (data == null)
			throw new IllegalStateException("The data from the TestData object is null. cannot run the send method.");

		synchronized (this) {
			while (data != null) {

				while (this.lastFrameSend < this.lastFrameAck + this.windowSize && data != null) {
					Frame frame = new Frame(this.address, this.destinationAddress, this.lastFrameSend, data, false,
							false);
					FrameSenderThread fs = new FrameSenderThread(frame, this.networkCard, this.timeOut);

					data = td.getTestData();

					if (data == null) {
						frame.setTerminating(true);
						frame.recalculateChecksum();
					}

					this.lastFrameSend++;

					this.buffer.add(fs);

					fs.start();
				}

				wait();
			}
		}
	}

	@Override
	public void receive(byte[] arg0) {
		Frame ackFrame = new Frame(arg0);
		
		if(receivedFrameIsValid(ackFrame) == false) {
			return;
		}

		synchronized (this) {

			// Terminate if the terminating acknowledge is received                                                               
			if (ackFrame.isTerminating()) {
				System.out.println(Helper.GetMilliTime() + ": Received acknowledge for terminating frame " + ackFrame.getSequenceNumber());
				System.out.println("Terminating");
				System.exit(0);
			}

			System.out.println(Helper.GetMilliTime() + ": Received acknowledge for frame " + ackFrame.getSequenceNumber());
			
			this.lastFrameAck = ackFrame.getSequenceNumber();

			for (int i = 0; i < buffer.size(); i++) {
				if (this.buffer.get(i).getFrame().getSequenceNumber() <= this.lastFrameAck) {
					this.buffer.get(i).setAcknowledged(true);
					this.buffer.remove(i);
				}
			}

			notify();
			
		}

	}
	
	private boolean receivedFrameIsValid(Frame frame) {
		if(frame.isValid() && frame.getDestinationAddress() == this.address && frame.isAcknowledge() == true) {
			return true;
		}
		
		// If the frame is not valid, try to print its checksum
		System.out.print(Helper.GetMilliTime() + ": Received invalid frame. ");
		try {
			System.out.println("Checksum: " + frame.getCheckSum());
		} catch (Exception e) {
			System.out.println("Can't read invalid frame");
		}
		
		return false;
	}

}
