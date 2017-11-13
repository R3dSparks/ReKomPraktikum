
import rn.TestData;

public class Tester {

	public static void main(String[] args) {
		
		TestData td = TestData.createTestData(1);
		
		Frame f = null;
		
		int i = 0;
		
		byte[] data = td.getTestData();
		
		while(data != null) {
			f = new Frame(1234, 4321, i++, data, false, false);
			data = td.getTestData();
		}
		
		Frame r = new Frame(f.GetBytes());
		
		System.out.println(r);
		
		
	}
	
}
