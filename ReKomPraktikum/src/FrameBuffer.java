
public class FrameBuffer {

	private Frame[] buffer;
	private int size;
	private int pointer = 0;
	
	
	public FrameBuffer(int size) {
		this.size = size;
		this.buffer = new Frame[size];
	}
	
	
	public boolean AddFrame(Frame frame) {

		int position = frame.getSequenceNumber() - this.pointer;
		
		if(position < this.size) {
			this.buffer[(this.pointer + position) % this.size] = frame;

			return true;
		}

		
		return false;
		
	}
	
	/**
	 * Get the next Frame from the buffer
	 * @return
	 * Next Frame or null if next position is empty
	 */
	public Frame GetNextFrame() {
		
		Frame frame = this.buffer[this.pointer % this.size];
		
		if(frame != null) {
			// Remove current Frame from the buffer and increment the pointer
			this.buffer[this.pointer % this.size] = null;
			this.pointer++;
		}
		
		return frame;
	}
	
	public boolean ContainsFrame(Frame frame) {
		
		for(int i = 0; i < this.size; i++) {
			if(this.buffer[i] != null && this.buffer[i].getSequenceNumber() == frame.getSequenceNumber()) {
				return true;
			}
		}
		
		return false;
	}
	
	
	
}
