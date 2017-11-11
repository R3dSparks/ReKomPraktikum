
public class Tester {

	public static void main(String[] args) {
		
		Frame f = new Frame(1234, 4321, 99, new byte[0], false, false);
		
		Frame r = new Frame(f.GetBytes());
			
		r.CheckFrame();
	}
	
}
