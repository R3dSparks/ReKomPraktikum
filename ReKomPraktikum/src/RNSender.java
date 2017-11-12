import java.io.IOException;

import rn.TestData;

public class RNSender {

	public static void main(String[] args) {

		try {

			checkStartupArguments(args);

			short sourceAdress = Short.parseShort(args[0]);
			short destinationAdress = Short.parseShort(args[1]);
			int windowSize = Integer.parseInt(args[2]);
			int testData = Integer.parseInt(args[3]);
			TestData td = TestData.createTestData(testData);

			FrameSender sender = new FrameSender(sourceAdress, destinationAdress, windowSize);
			td.writeToFile("data.in");
			sender.send(td);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			System.out.println(String.format("There was a problem with the startup arguments: %s\n%s", e.getMessage(),
					e.getStackTrace()));
		} catch (Exception e) {
			System.out.println(String.format("An unexpected error occurred with the following message: %s\n%s",
					e.getMessage(), e.getStackTrace()));
		}
	}

	/**
	 * check if the startup arguments are valid
	 * 
	 * @param args
	 */
	private static void checkStartupArguments(String[] args) {

		if (args == null)
			throw new IllegalArgumentException("The given startup arguments are null.");

		if (args.length < 4)
			throw new IllegalArgumentException("There must be at least 4 startup arguments for this sender to run.");

		if (Helper.tryParceShort(args[0]) == false)
			throw new IllegalArgumentException(
					"The first startup argument is not a number or the size is too big (between -32.768 and 32.767). Its used as a a source address");

		if (Helper.tryParceShort(args[1]) == false)
			throw new IllegalArgumentException(
					"The second startup argument is not a number or the size is too big (between -32.768 and 32.767). Its used as a destination address");

		if (Helper.tryParceInt(args[2]) == false)
			throw new IllegalArgumentException(
					"The third startup argument is not a number or the size is too big (between –2.147.483.648 and 2.147.483.647). Its used for the window size.");

		if (Helper.tryParceInt(args[3]) == false)
			throw new IllegalArgumentException(
					"The fourth startup argument is not a number or the size is too big (between –2.147.483.648 and 2.147.483.647). Its used as test data.");
		// TODO
		// gescheite beschreibung von der letzten exception
	}

}
