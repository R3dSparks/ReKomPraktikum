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

		// check parameters
		if (parametrsAreValid(arg0) == false)
			return;

		// clean terminator and read frame data
		cleanUpTerminator();
		Frame dataFrame = new Frame(arg0);

		// check if the frame is valid
		if (receivedFrameIsValid(dataFrame) == false)
			return;

		// TODO soll das wirklich so sein? geht nicht auch folgendes:
		// this.terminating = dataFrame.isTerminating();
		if (dataFrame.isTerminating()) {
			this.terminating = true;
		}

		// Resending acknowledge
		if (dataFrame.getSequenceNumber() <= this.lastFrameAcknowledged) {

			Frame ackFrame = new Frame(this.adress, dataFrame.getSequenceNumber(), this.lastFrameAcknowledged,
					new byte[0], true, dataFrame.isTerminating());

			System.out.println(Helper.GetMilliTime() + ": Resending acknowledge for " + ackFrame.getSequenceNumber()
					+ " to " + ackFrame.getDestinationAddress());

			this.send(ackFrame);

		} else
		// Sending acknowledge for new frame within window size
		if (dataFrame.getSequenceNumber() <= this.lastFrameAcknowledged + this.windowSize) {

			System.out.println(Helper.GetMilliTime() + ": this.Received frame " + dataFrame.getSequenceNumber());

			this.buffer.AddFrame(dataFrame);

			Frame writeFrame = this.buffer.GetNextFrame();

			while (writeFrame != null) {
				writeLine(writeFrame.getPayload());
				this.lastFrameAcknowledged++;
				writeFrame = this.buffer.GetNextFrame();
			}

			Frame ackFrame = new Frame(this.adress, dataFrame.getSequenceNumber(), this.lastFrameAcknowledged,
					new byte[0], true, dataFrame.isTerminating());

			this.send(ackFrame);

			System.out.println(Helper.GetMilliTime() + ": Sending acknowledge for " + ackFrame.getSequenceNumber()
					+ " to " + ackFrame.getDestinationAddress());
		}

		if (this.terminating) {
			this.terminator = new TimedTerminator(1000);
			this.terminator.start();
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

	/**
	 * check if the input parameters for the receive method are valid
	 * 
	 * @param arg0
	 * @return
	 */
	private boolean parametrsAreValid(byte[] arg0) {
		if (arg0 == null) {
			System.out.println("The received data is null. cannot process any further");
			return false;
		}

		if (arg0.length == 0) {
			System.out.println("The received data length is 0. cannot process any further");
			return false;
		}
		return true;
	}

	/**
	 * if the terminator is not null, then interrupt it and set it to null
	 */
	private void cleanUpTerminator() {
		if (this.terminator != null) {
			this.terminator.interrupt();
			this.terminator = null;
		}
	}

	/**
	 * check if the received frame is valid
	 * 
	 * @param dataFrame
	 * @return
	 */
	private boolean receivedFrameIsValid(Frame dataFrame) {
		if (dataFrame.CheckFrame() && dataFrame.getDestinationAddress() == this.adress
				&& dataFrame.getSequenceNumber() == this.sourceAdress) {
			System.out.println(Helper.GetMilliTime() + ": Received invalid frame!");
			try {
				System.out.println("Sequence number: " + dataFrame.getCheckSum());
			} catch (Exception e) {
				System.out.println("Can't read invalid frame");
			}
			return false;
		}
		return true;
	}

}
