package Parser;

import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;

public class FileReader {
	
	public FileReader(){
		
	}
	
	public static Scanner reader;
	
	public boolean openFile(String path){
		try {
			FileInputStream file = new FileInputStream(new File(path));
			reader = new Scanner(file);
			return true;
		} catch (Exception e){
			return false;
		}
	}
	
	public String readLine() {
		if (reader == null)
			return null;
		if (!reader.hasNext())
			return null;
		else
			return reader.nextLine();
	}
	
	public static void closeFile(){
		if (reader == null)
			return;
		reader.close();
		reader = null;
	}
	
	public static String comboPaths(String[] args){
		StringBuilder sb = new StringBuilder();
		sb.append(args[0]);
		for (int i = 1; i<args.length; i++){
			sb.append("//"+args[i]);
		}
		return sb.toString();
	}

}
