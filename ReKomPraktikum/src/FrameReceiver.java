import java.io.File;
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

		// Check parameters
		if (parametrsAreValid(arg0) == false)
			return;

		// Read frame data
		cleanUpTerminator();
		Frame dataFrame = new Frame(arg0);

		// Check if the frame is valid
		if (receivedFrameIsValid(dataFrame) == false) {
			if (this.terminating) {
				this.terminator = new TimedTerminator(1000);
				this.terminator.start();
			}

			return;
		}

		// Set terminating flag true if received frame is terminating and is the
		// last frame to be acknowledged
		if (dataFrame.isTerminating() && dataFrame.getSequenceNumber() == this.lastFrameAcknowledged + 1) {
			this.terminating = true;
			this.lastFrameAcknowledged++;
		}

		// Resending acknowledge
		if (dataFrame.getSequenceNumber() <= this.lastFrameAcknowledged) {

			Frame ackFrame = new Frame(this.adress, this.sourceAdress, this.lastFrameAcknowledged, new byte[0], true,
					this.terminating);

			System.out.println(Helper.GetMilliTime() + ": Resending acknowledge for frame "
					+ ackFrame.getSequenceNumber() + " to " + ackFrame.getDestinationAddress());

			this.send(ackFrame);

		} else
		// Sending acknowledge for new frame within window size
		if (dataFrame.getSequenceNumber() <= this.lastFrameAcknowledged + this.windowSize) {

			if (dataFrame.isTerminating() == true) {
				System.out.println(Helper.GetMilliTime() + ": Received terminating frame");
			} else {
				System.out.println(Helper.GetMilliTime() + ": Received frame " + dataFrame.getSequenceNumber());

				this.buffer.AddFrame(dataFrame);

				// Get the next frame that is to be written
				Frame writeFrame = this.buffer.GetNextFrame();

				while (writeFrame != null) {
					writeLine(writeFrame.getPayload());
					this.lastFrameAcknowledged++;
					writeFrame = this.buffer.GetNextFrame();
				}
			}

			Frame ackFrame = new Frame(this.adress, this.sourceAdress, this.lastFrameAcknowledged, new byte[0], true,
					this.terminating);

			this.send(ackFrame);

			if (this.terminating == true) {
				System.out.println(
						Helper.GetMilliTime() + ": Sending termination to " + ackFrame.getDestinationAddress());
			} else {
				System.out.println(Helper.GetMilliTime() + ": Sending acknowledge for " + ackFrame.getSequenceNumber()
						+ " to " + ackFrame.getDestinationAddress());
			}

		}

		if (this.terminating) {
			this.terminator = new TimedTerminator(1000);
			this.terminator.start();
		}

	}

	private void writeLine(byte[] data) {

		FileOutputStream fos = null;
		File file = null;
		try {

			file = new File(this.savePath);

			// make sure that the file exists
			if (file.exists() == false)
				file.createNewFile();

			// append the data to the file
			fos = new FileOutputStream(file, true);
			fos.write(data);
			fos.close();

		} catch (IOException e) {
			System.out.println("Failed to write to output. Trying again.");
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					System.out.println("Closing output stream failed");
				}
			}

		}

	}

	/**
	 * Check if the input parameters for the receive method are valid
	 * 
	 * @param arg0
	 * @return
	 */
	private boolean parametrsAreValid(byte[] arg0) {
		if (arg0 == null) {
			System.out.println("The received data is null. Cannot process any further");
			return false;
		}

		if (arg0.length == 0) {
			System.out.println("The received data length is 0. Cannot process any further");
			return false;
		}
		return true;
	}

	/**
	 * If the terminator is not null, then interrupt it and set it to null
	 */
	private void cleanUpTerminator() {
		if (this.terminator != null) {
			this.terminator.interrupt();
			this.terminator = null;
		}
	}

	/**
	 * Check if the received frame is valid
	 * 
	 * @param dataFrame
	 * @return
	 */
	private boolean receivedFrameIsValid(Frame dataFrame) {
		// Return true if frame is valid, the destination address is correct and
		// the acknowledge flag is false
		if (dataFrame.isValid() && dataFrame.getDestinationAddress() == this.adress
				&& dataFrame.isAcknowledge() == false) {
			return true;
		}

		// If the frame is not valid, try to print its checksum
		System.out.print(Helper.GetMilliTime() + ": Received invalid frame. ");
		try {
			System.out.println("Checksum: " + dataFrame.getCheckSum());
		} catch (Exception e) {
			System.out.println("Can't read invalid frame");
		}
		return false;
	}

}
