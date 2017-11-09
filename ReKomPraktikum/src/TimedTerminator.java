
public class TimedTerminator extends Thread{
	
	private int m_timeOut;
	
	public TimedTerminator(int timeOut) {
		this.m_timeOut = timeOut;
	}
	
	@Override
	public void run() {
		try {
			waitForTermination();
		} catch (InterruptedException e) {
			System.out.println("Canceled termination");
		}
	}
	
	private void waitForTermination() throws InterruptedException {
		System.out.println("Started termination timer");
		
		Thread.sleep(m_timeOut);
		
		System.out.println("Terminating");
		System.exit(0);
	}

}
