package Parser;

import org.joda.time.DateTime;

/**
 * <p>
 * Base class for all RF2 items.
 * </p>
 * 
 * @author Travis Lukach
 */
public class RF2Item {
	/**
	 * <p>
	 * Parser that created this node.
	 * </p>
	 */
	private RF2Parser parser;

	/**
	 * <p>
	 * Snomed id.
	 * </p>
	 */
	private long id;

	/**
	 * <p>
	 * Gets <i>parser</i>.
	 * </p>
	 * 
	 * @return
	 */
	public RF2Parser getParser() {
		return parser;
	}

	/**
	 * <p>
	 * Sets <i>parser</i>.
	 * </p>
	 * 
	 * @param parser
	 */
	public void setParser(RF2Parser parser) {
		this.parser = parser;
	}

	/**
	 * <p>
	 * Gets <i>id</i>.
	 * </p>
	 * 
	 * @return
	 */
	public long getId() {
		return id;
	}

	/**
	 * <p>
	 * Sets <i>id</i>.
	 * </p>
	 * 
	 * @param id
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * <p>
	 * Parse string into boolean value.
	 * </p>
	 * 
	 * @param parser
	 * @param line
	 * @return
	 */
	public static boolean parseBool(String s) {
		switch (s) {
			case "0" :
				return false;
			case "1" :
				return true;
			default :
				throw new UnsupportedOperationException(
						"Invalid boolean value");
		}
	}

	/**
	 * <p>
	 * Parse effective time into c# date time instance.
	 * </p>
	 * 
	 * @param s
	 * @return
	 */
	public static DateTime parseEffectiveTime(String s) {
		if (s.length() != 8) {
			throw new RuntimeException("Expected date of format YYYYMMDD");
		}

		return new DateTime();
	}

}