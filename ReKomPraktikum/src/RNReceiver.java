import java.io.IOException;

public class RNReceiver {

	public static void main(String[] args) {

		try {

			if (args == null)
				throw new IllegalArgumentException("The given startup arguments are null.");

			if (args.length < 3)
				throw new IllegalArgumentException(
						"There must be at least 3 startup arguments for this receiver to run.");

			if (Helper.tryParceShort(args[0]) == false)
				throw new IllegalArgumentException(
						"The first startup argument is not a number or the size is too big (between -32.768 and 32.767). Its used as a a source address");

			if (Helper.tryParceShort(args[1]) == false)
				throw new IllegalArgumentException(
						"The second startup argument is not a number or the size is too big (between -32.768 and 32.767). Its used as a destination address");

			if (Helper.tryParceInt(args[2]) == false)
				throw new IllegalArgumentException(
						"The third startup argument is not a number or the size is too big (between –2.147.483.648 and 2.147.483.647). Its used for the window size.");

			short sourceAddress = Short.parseShort(args[0]);
			short destinationAddress = Short.parseShort(args[1]);
			int windowSize = Integer.parseInt(args[2]);

			FrameReceiver receiver = new FrameReceiver(sourceAddress, destinationAddress, windowSize);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			System.out.println(String.format("There was a problem with the startup arguments: %s\n%s", e.getMessage(),
					e.getStackTrace()));
		} catch (Exception e) {
			System.out.println(String.format("An unexpected error occurred with the following message: %s\n%s",
					e.getMessage(), e.getStackTrace()));
		}
	}

}
