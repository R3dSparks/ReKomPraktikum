
public class FrameBuffer {

	private Frame[] m_buffer;
	
	private int m_size;
	
	private int m_pointer = 0;
	
	
	public FrameBuffer(int size) {
		m_size = size;
		m_buffer = new Frame[size];
	}
	
	
	public boolean AddFrame(Frame frame) {

		int position = frame.SequenceNumber - m_pointer;
		
		if(position < m_size) {
			m_buffer[(m_pointer + position) % m_size] = frame;

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
		
		Frame frame = m_buffer[m_pointer % m_size];
		
		if(frame != null) {
			// Remove current Frame from the buffer and increment the pointer
			m_buffer[m_pointer % m_size] = null;
			m_pointer++;
		}
		
		return frame;
	}
	
	public boolean ContainsFrame(Frame frame) {
		
		for(int i = 0; i < m_size; i++) {
			if(m_buffer[i] != null && m_buffer[i].SequenceNumber == frame.SequenceNumber) {
				return true;
			}
		}
		
		return false;
	}
	
	
	
}
