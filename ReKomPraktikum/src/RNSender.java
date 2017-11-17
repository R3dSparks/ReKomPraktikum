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
			// Network card exception
			System.out.println(String.format("There was a problem with the network card with the message: %s\n%s",
					e.getMessage(), e.getStackTrace()));
		} catch (InterruptedException e) {
			// Thread exception
			System.out.println(String.format("There was a problem with a sender thread with the message: %s\n%s",
					e.getMessage(), e.getStackTrace()));
		} catch (IllegalStateException e) {
			// Exception within the sender
			System.out.println(String.format("There was a problem within the sender with the message: %s\n%s",
					e.getMessage(), e.getStackTrace()));
		} catch (IllegalArgumentException e) {
			// Startup argument exception
			System.out.println(String.format("There was a problem with the startup arguments with the message: %s\n%s",
					e.getMessage(), e.getStackTrace()));
		} catch (Exception e) {
			// Unknown exception
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

		if (Helper.tryParseShort(args[0]) == false)
			throw new IllegalArgumentException(
					"The source address argument is not a number or the size is too big (between -32.768 and 32.767). Its used as a a source address");

		if (Helper.tryParseShort(args[1]) == false)
			throw new IllegalArgumentException(
					"The destination address argument is not a number or the size is too big (between -32.768 and 32.767). Its used as a destination address");

		if (Helper.tryParseInt(args[2]) == false)
			throw new IllegalArgumentException(
					"The window size argument is not a number or the size is too big (between –2.147.483.648 and 2.147.483.647). Its used for the window size.");

		if (Helper.tryParseInt(args[3]) == false)
			throw new IllegalArgumentException(
					"The test data argument is not a number or the size is too big (between –2.147.483.648 and 2.147.483.647). Its used as test data.");

		if (Integer.parseInt(args[2]) < 1)
			throw new IllegalArgumentException("The window size has to be at least 1.");

		if (Integer.parseInt(args[3]) < 0 || Integer.parseInt(args[3]) > 1)
			throw new IllegalArgumentException(
					"Illegal argument for test data. The only valid test data id's are 0 or 1.");
	}

}
