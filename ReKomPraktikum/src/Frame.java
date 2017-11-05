
public class Frame {
	
	// Header length in 16 bit blocks
	private static int headerLength = 6;

	public short SourceAddress;

	public short DestinationAddress;
	
	public short SequenceNumber;
	
	public boolean Acknowledge;
	
	public boolean Terminating;
	
	public short Checksumm = 0;
	
	public short PayloadLength;
	
	public byte[] Payload;
	
	private boolean isValid = true;
	
	
	public Frame(int sourceAddress, int destinationAddress, int sequenceNumber, byte[] payload, boolean ack, boolean term) {
		this.SourceAddress = (short)sourceAddress;
		this.DestinationAddress = (short)destinationAddress;
		this.SequenceNumber = (short)sequenceNumber;
		this.Payload = payload;
		this.Acknowledge = ack;
		this.Terminating = term;
		this.PayloadLength = (short)payload.length;
		
		Checksumm = getChecksumm();
	}
	
	public Frame(byte[] data) {
		try {
			SourceAddress |= (data[0] & 0x00ff) << 8;
			SourceAddress |= data[1] & 0x00ff;
			
			DestinationAddress |= (data[2] & 0x00ff) << 8;
			DestinationAddress |= data[3] & 0x00ff;			
			
			SequenceNumber |= (data[4] & 0x00ff) << 8;
			SequenceNumber |= data[5] & 0x00ff;
			
			Acknowledge = (data[7] & 0x01) > 0;
			
			Terminating = (data[7] & 0x02) > 0;

			Checksumm |= (data[8] & 0x00ff) << 8;
			Checksumm |= data[9] & 0x00ff;
			
			PayloadLength |= (data[10] & 0x00ff) << 8;
			PayloadLength |= data[11] & 0x00ff;
			
			Payload = new byte[PayloadLength];
			
			for(int i = 0; i < PayloadLength; i++) {
				Payload[i] = data[headerLength * 2 + i];			
			}
		}
		catch(Exception e) {
			this.isValid = false;
		}
	}
	
	
	private short[] getHeader() {
		short[] header = new short[headerLength];
		
		header[0] = SourceAddress;
		header[1] = DestinationAddress;
		header[2] = SequenceNumber;
		header[3] = (short)((Acknowledge ? 1 : 0) | (Terminating ? 1 : 0) << 1);
		header[4] = Checksumm;
		header[5] = (short)Payload.length;
						
		return header;
		
	}
	
	private short getChecksumm() {
		
		int checksumm = 0;
		
		short[] header = getHeader();
		
		for(int i = 0; i < header.length; i++) {
			checksumm += header[i];
		}
		
		for(int i = 0; i < Payload.length; i++) {
			checksumm += Payload[i];
		}
		
		// Add overflow of short summ to checksumm
		while((checksumm & 0xffff0000) != 0) {
			int overflow = ((checksumm & 0xffff0000) >> 16) & 0x0000ffff;
		
			checksumm = checksumm & 0x0000ffff;
			
			checksumm += overflow;
		}
				
		checksumm ^= 0x0000ffff;
		
		return (short)checksumm;
	}
	
	public byte[] GetBytes() {
		byte[] frame = new byte[headerLength * 2 + Payload.length];
		
		short[] header = getHeader();

		for(int i = 0; i < headerLength; i++) {
			frame[i * 2 + 1] = (byte)(header[i] & 0x00ff);
			frame[i * 2] = (byte)((header[i] & 0xff00) >> 8);
		}
		
		for(int i = 0; i < Payload.length; i++) {
			frame[headerLength * 2 + i] = Payload[i];
		}
		
		return frame;
	}
	
	public boolean CheckFrame() {		
		return this.isValid && getChecksumm() == 0;
	}
	
	@Override
	public String toString() {
		
		String temp = "";
		
		short[] header = getHeader();
		
		for(int i = 0; i < header.length; i++) {
			temp += String.format("%16s", Integer.toHexString(header[i])).replace(" ", "0") + "\n";
		}
		
		for(int i = 0; i < Payload.length; i++) {
			temp += String.format("%16s", Integer.toHexString(Payload[i])).replace(" ", "0") + "\n";
		}
		
		return temp;
	}




}
