package Parser;

/**
 * Main program for parser. Creates parser object and runs it.
 * @author Travis Lukach
 */
public class Program {
	/**
	 * <p>
	 * Main method of program.
	 * </p>
	 * @param args
	 */
	public static void main(final String[] args) {
		SnomedParser sp = new SnomedParser();
		sp.parseAndSerialize();
	}
}