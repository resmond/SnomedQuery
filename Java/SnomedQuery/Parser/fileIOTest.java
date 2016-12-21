package Parser;

import java.io.File;

import org.junit.Test;

public class fileIOTest {

	
	@Test
	public void currentFile(){
		File file = new File("here");
		System.out.println(file.getAbsolutePath());
	}
}
