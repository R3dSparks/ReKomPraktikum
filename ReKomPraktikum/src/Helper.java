public final class Helper {

	private static long baseTime = System.currentTimeMillis();

	private Helper() {

	}
	
	public static String GetMilliTime() {
		return String.valueOf(System.currentTimeMillis() - baseTime);
	}

	/**
	 * Check if the given string can be converted into a short value
	 * 
	 * @param strShort
	 *            check if this string is a valid short
	 * @return
	 */
	public static boolean tryParseShort(String strShort) {
		try {
			Short.parseShort(strShort);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	/**
	 * Check if the given string can be converted into a integer value
	 * 
	 * @param strInt
	 *            check if this string is a valid integer
	 * @return
	 */
	public static boolean tryParseInt(String strInt) {
		try {
			Integer.parseInt(strInt);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

}
