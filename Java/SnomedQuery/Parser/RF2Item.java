package Parser;

import org.joda.time.DateTime;

/**
 * Base class for all RF2 items.
 */
public class RF2Item {
	/**
	 * Parser that created this node.
	 */
	public RF2Parser Parser;

	/**
	 * Snomed id.
	 */
	public long Id;

	/**
	 * Parse string into boolean value.
	 * 
	 * @param parser
	 * @param line
	 * @return
	 */
	public static boolean ParseBool(String s) {
		switch (s) {
		case "0":
			return false;
		case "1":
			return true;
		default:
			throw new UnsupportedOperationException("Invalid boolean value");
		}
	}

	/**
	 * Parse effective time into c# date time instance.
	 * 
	 * @param s
	 * @return
	 */
	public static DateTime ParseEffectiveTime(String s) {
		if (s.length() != 8) {
			throw new RuntimeException("Expected date of format YYYYMMDD");
		}

		return new DateTime();
	}
}