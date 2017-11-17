import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RNReceiver {

	public static void main(String[] args) {

		try {

			checkStartupArguments(args);

			short sourceAddress = Short.parseShort(args[0]);
			short destinationAddress = Short.parseShort(args[1]);
			int windowSize = Integer.parseInt(args[2]);
			
			//TODO delete after debugging
			Files.deleteIfExists(Paths.get("data.out"));

			@SuppressWarnings("unused")
			FrameReceiver receiver = new FrameReceiver(sourceAddress, destinationAddress, windowSize);
		} catch (IOException e) {
			// network card exception
			System.out.println(String.format("There was a problem with the network card with the message: %s\n%s",
					e.getMessage(), e.getStackTrace()));
		} catch (IllegalArgumentException e) {
			// startup argument exception
			System.out.println(String.format("There was a problem with the startup arguments with the message: %s\n%s",
					e.getMessage(), e.getStackTrace()));
		} catch (Exception e) {
			// unknown exception
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

		if (args.length < 3)
			throw new IllegalArgumentException("There must be at least 3 startup arguments for this receiver to run.");

		if (Helper.tryParseShort(args[0]) == false)
			throw new IllegalArgumentException(
					"The source address argument is not a number or the size is too big (between -32.768 and 32.767). Its used as a a source address");

		if (Helper.tryParseShort(args[1]) == false)
			throw new IllegalArgumentException(
					"The destination address argument is not a number or the size is too big (between -32.768 and 32.767). Its used as a destination address");

		if (Helper.tryParseInt(args[2]) == false)
			throw new IllegalArgumentException(
					"The window size argument is not a number or the size is too big (between –2.147.483.648 and 2.147.483.647). Its used for the window size.");

		if(Integer.parseInt(args[2]) < 1)
			throw new IllegalArgumentException(
					"The window size has to be at least 1.");
	}

}
