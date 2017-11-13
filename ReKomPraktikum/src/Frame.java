
public class Frame {

	// Header length in 16 bit blocks
	private static int headerLength = 6;

	private short sourceAddress;
	private short destinationAddress;
	private short sequenceNumber;
	private boolean acknowledge;
	private boolean terminating;
	private short checksum = 0;
	private short payloadLength;
	private byte[] payload;
	private boolean isValid = true;

	/**
	 * Create a Frame by values
	 * 
	 * @param sourceAddress
	 * @param destinationAddress
	 * @param sequenceNumber
	 * @param payload
	 * @param ack
	 * @param term
	 */
	public Frame(int sourceAddress, int destinationAddress, int sequenceNumber, byte[] payload, boolean ack, boolean term) {
		this.sourceAddress = (short) sourceAddress;
		this.destinationAddress = (short) destinationAddress;
		this.sequenceNumber = (short) sequenceNumber;
		this.payload = payload;
		this.acknowledge = ack;
		this.terminating = term;
		this.payloadLength = (short) payload.length;

		this.checksum = calculateChecksum();
	}

	/**
	 * Create a Frame by Bytes
	 * 
	 * @param data
	 */
	public Frame(byte[] data) {
		try {
			this.sourceAddress |= (data[0] & 0x00ff) << 8;
			this.sourceAddress |= data[1] & 0x00ff;

			this.destinationAddress |= (data[2] & 0x00ff) << 8;
			this.destinationAddress |= data[3] & 0x00ff;

			this.sequenceNumber |= (data[4] & 0x00ff) << 8;
			this.sequenceNumber |= data[5] & 0x00ff;

			this.acknowledge = (data[7] & 0x01) > 0;

			this.terminating = (data[7] & 0x02) > 0;

			this.checksum |= (data[8] & 0x00ff) << 8;
			this.checksum |= data[9] & 0x00ff;

			this.payloadLength |= (data[10] & 0x00ff) << 8;
			this.payloadLength |= data[11] & 0x00ff;

			this.payload = new byte[this.payloadLength];

			for (int i = 0; i < this.payloadLength; i++) {
				this.payload[i] = data[headerLength * 2 + i];
			}
			
			if(this.payload.length != this.payloadLength)
				this.isValid = false;
				
			this.checksum = calculateChecksum();
			
		} catch (Exception e) {
			this.isValid = false;
		}
	}

	/**
	 * convert a Frame into a byte array
	 * 
	 * @return
	 */
	public byte[] GetBytes() {
		byte[] frame = new byte[headerLength * 2 + this.payload.length];

		short[] header = getHeader();

		for (int i = 0; i < headerLength; i++) {
			frame[i * 2 + 1] = (byte) (header[i] & 0x00ff);
			frame[i * 2] = (byte) ((header[i] & 0xff00) >> 8);
		}

		for (int i = 0; i < this.payload.length; i++) {
			frame[headerLength * 2 + i] = this.payload[i];
		}

		return frame;
	}

	@Override
	public String toString() {

		String temp = "";

		short[] header = getHeader();

		for (int i = 0; i < header.length; i++) {
			temp += String.format("%16s", Integer.toHexString(header[i])).replace(" ", "0") + "\n";
		}

		for (int i = 0; i < this.payload.length; i++) {
			temp += String.format("%16s", Integer.toHexString(this.payload[i])).replace(" ", "0") + "\n";
		}

		return temp;
	}

	private short[] getHeader() {
		short[] header = new short[headerLength];

		header[0] = this.sourceAddress;
		header[1] = this.destinationAddress;
		header[2] = this.sequenceNumber;
		header[3] = (short) ((this.acknowledge ? 1 : 0) | (this.terminating ? 1 : 0) << 1);
		header[4] = this.checksum;
		header[5] = (short) this.payload.length;

		return header;

	}

	private short calculateChecksum() {

		int checksumm = 0;

		short[] header = getHeader();

		for (int i = 0; i < header.length; i++) {
			checksumm += header[i] & 0x0000ffff;
		}

		for (int i = 0; i < this.payload.length; i++) {
			checksumm += this.payload[i] & 0x000000ff;
		}

		// Add overflow of short sum to checksum
		while ((checksumm & 0xffff0000) != 0) {
			int overflow = ((checksumm & 0xffff0000) >> 16) & 0x0000ffff;

			checksumm = checksumm & 0x0000ffff;

			checksumm += overflow;
		}

		checksumm ^= 0x0000ffff;

		return (short) checksumm;
	}
	
	/**
	 * Returns whether this frame is valid or not
	 * 
	 * @return
	 */
	public boolean isValid() {
		if(this.isValid == false) {
			return false;
		}
		
		return this.checksum == 0;
	}

	public void recalculateChecksum() {
		this.checksum = 0;
		this.checksum = calculateChecksum();
	}
	
	//
	// Getter and Setter
	//

	public short getSequenceNumber() {
		return this.sequenceNumber;
	}

	public short getDestinationAddress() {
		return this.destinationAddress;
	}

	public void setDestinationAddress(short destinationAddress) {
		this.destinationAddress = destinationAddress;
	}

	public boolean isAcknowledge() {
		return this.acknowledge;
	}

	public byte[] getPayload() {
		return this.payload;
	}

	public short getCheckSum() {
		return this.checksum;
	}

	public boolean isTerminating() {
		return this.terminating;
	}

	public void setTerminating(boolean terminating) {
		this.terminating = terminating;
	}

}
