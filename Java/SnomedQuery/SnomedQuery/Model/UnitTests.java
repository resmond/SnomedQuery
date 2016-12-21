package SnomedQuery.Model;

import org.junit.Test;

import Parser.SnomedParser;

public class UnitTests {
	
	@Test
	public void Parser(){
		SnomedParser parser = new SnomedParser();
		parser.ParseAndSerialize();
	}
}
