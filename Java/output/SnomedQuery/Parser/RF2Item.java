package SnomedQuery.Parser;

  /** 
   Base class for all RF2 items.
  */
  public class RF2Item
  {
	/** 
	 Parser that created this node.
	*/
	public RF2Parser Parser;

	/** 
	 Snomed id.
	*/
	public long Id;

	/** 
	 Parse string into boolean value.
	 
	 @param parser
	 @param line
	 @return 
	*/
	public static boolean ParseBool(String s)
	{
	  switch (s)
	  {
		case "0":
			return false;
		case "1":
			return true;
		default:
			throw new UnsupportedOperationException("Invalid boolean value");
	  }
	}

	/** 
	 Parse effective time into c# date time instance.
	 
	 @param s
	 @return 
	*/
	public static java.time.LocalDateTime ParseEffectiveTime(String s)
	{
	  if (s.length() != 8)
	  {
		throw new RuntimeException("Expected date of format YYYYMMDD");
	  }

	  return java.time.LocalDateTime.of(Integer.parseInt(s.substring(0, 4)), Integer.parseInt(s.substring(4, 6)), Integer.parseInt(s.substring(6, 8)));
	}
  }