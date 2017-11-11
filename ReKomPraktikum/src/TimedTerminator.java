
public class TimedTerminator extends Thread{
	
	private int timeOut;
	
	public TimedTerminator(int timeOut) {
		this.timeOut = timeOut;
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
		
		Thread.sleep(this.timeOut);
		
		System.out.println("Terminating");
		System.exit(0);
	}

}
