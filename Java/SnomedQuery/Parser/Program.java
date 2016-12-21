package Parser;

  /** 
   Main program for parser. Creates parser object and runs it.
  */
  public class Program
  {
	/** 
	 Main method of program.
	 
	 @param args
	*/
	static void main(String[] args)
	{
	  SnomedParser sp = new SnomedParser();
	  sp.ParseAndSerialize();
	}
  }