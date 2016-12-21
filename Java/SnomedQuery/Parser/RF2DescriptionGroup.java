package Parser;

  /** 
   Group of descriptions, all with same SNOMED id.
  */
  public class RF2DescriptionGroup extends TRF2ItemGroup<RF2Description>
  {
	/** 
	 Constructor
	*/
	public RF2DescriptionGroup()
	{
	}

	/** 
	 Add RF2Description to description group.
	 
	 @param description
	*/
	public final void AddDescription(RF2Description description)
	{
	  this.items.add(description);
	}

	/** 
	 Get active description. Try to get english, but take anyone if english
	 not found.
	 
	 @return 
	*/
	public final RF2Description GetActiveEnglishDescription()
	{
	  RF2Description desciptionAny = null;
	  for (RF2Description description : this.getItems())
	  {
		if (description.LanguageCode.equals("en"))
		{
		  return description;
		}
		desciptionAny = description;
	  }
	  return desciptionAny;
	}

	/** 
	 Override ToString() method.
	 
	 @return 
	*/
	@Override
	public String toString()
	{
	  return "RF2DescriptionGroup [{Id}] Descriptions Count: {descriptions.Count}";
	}
  }