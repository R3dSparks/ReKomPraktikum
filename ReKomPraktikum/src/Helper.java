
public final class Helper {

	private static long baseTime = System.currentTimeMillis();
	
	private Helper() {
		
	}
	
	public static String GetMilliTime() {
		return String.valueOf(System.currentTimeMillis() - baseTime);
	}
	
}
