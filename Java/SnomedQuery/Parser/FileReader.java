package Parser;

import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;
/**
 * <p>
 * Handler for file i/o.
 * </p>
 * 
 * @author Travis Lukach
 *
 */
public class FileReader {
	/**
	 * <p>
	 * Constructor.
	 * </p>
	 */
	public FileReader() {
	}

	/**
	 * <p>
	 * accessable Scanner Object.
	 * </p>
	 */
	private static Scanner reader;

	/**
	 * <p>
	 * Opens target file.
	 * </p>
	 * 
	 * @param path
	 * @return
	 */
	public final boolean openFile(final String path) {
		try {
			FileInputStream file = new FileInputStream(new File(path));
			reader = new Scanner(file, "UTF8");
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * <p>
	 * Returns a read line in a text file.
	 * <p>
	 * 
	 * @return
	 */
	public final String readLine() {
		if (reader == null) {
			return null;
		}
		if (!reader.hasNext()) {
			return null;
		} else {
			return reader.nextLine();
		}
	}

	/**
	 * <p>
	 * Closes the file.
	 * </p>
	 */
	public static void closeFile() {
		if (reader == null) {
			return;
		}
		reader.close();
		reader = null;
	}

	/**
	 * <p>
	 * Returns a file path built from the string array parameter
	 * </p>
	 * 
	 * @param args
	 * @return
	 */
	public static String comboPaths(final String[] args) {
		StringBuilder sb = new StringBuilder();
		sb.append(args[0]);
		for (int i = 1; i < args.length; i++) {
			sb.append("\\" + args[i]);
		}
		return sb.toString();
	}

}
