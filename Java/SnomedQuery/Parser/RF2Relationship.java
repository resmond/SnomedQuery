package Parser;

  /** 
   Stored data from one line of the RFS concept file.
  */
  public class RF2Relationship extends RF2ItemSingle
  {
	/** 
	 Snomed Id of relationship source
	*/
	public long SourceId;

	/** 
	 Snomed Id of relationship destination
	*/
	public long DestinationId;

	/** 
	 Snomed relationship group.
	*/
	public int RelationshipGroup;

	/** 
	 Snomed relationship type.
	*/
	public long TypeId;

	/** 
	 Snomed relationship characteristic type id.
	*/
	public long CharacteristicTypeId;

	/** 
	 Snomed modifier id.
	*/
	public long ModifierId;

	/** 
	 Lazy load TypeConcept.
	*/
	public final RF2ConceptGroup getTypeConcept()
	{
	  if (this.typeConcept == null)
	  {
		this.typeConcept = this.Parser.GetConceptGroup(this.TypeId);
	  }
	  return this.typeConcept;
	}

	/** 
	 Backing field for TypeConcept property.
	*/
	private RF2ConceptGroup typeConcept = null;

	/** 
	 Lazy load Source.
	*/
	public final RF2ConceptGroup getSource()
	{
	  if (this.source == null)
	  {
		this.source = this.Parser.GetConceptGroup(this.SourceId);
	  }
	  return this.source;
	}

	/** 
	 Backing field for Source property.
	*/
	private RF2ConceptGroup source = null;

	/** 
	 Lazy load Destination.
	*/
	public final RF2ConceptGroup getDestination()
	{
	  if (this.destination == null)
	  {
		this.destination = this.Parser.GetConceptGroup(this.DestinationId);
	  }
	  return this.destination;
	}

	/** 
	 Backing field for Destination property.
	*/
	private RF2ConceptGroup destination = null;

	/** 
	 Constructor
	*/
	public RF2Relationship()
	{
	}

	/** 
	 Parse single line from relationship rf2 file.
	 
	 @param parser
	 @param line
	 @return 
	*/
	public static RF2Relationship Parse(RF2Parser parser, String line)
	{
	  String[] parts = line.split("[\\t]", -1);
	  if (parts.length != 10)
	  {
		throw new RuntimeException("Invalid Relationship line");
	  }

	  RF2Relationship tempVar = new RF2Relationship();
	  tempVar.Parser = parser;
	  tempVar.Id = Long.parseLong(parts[0]);
	  tempVar.EffectiveTime = ParseEffectiveTime(parts[1]);
	  tempVar.Active = ParseBool(parts[2]);
	  tempVar.ModuleId = Long.parseLong(parts[3]);
	  tempVar.SourceId = Long.parseLong(parts[4]);
	  tempVar.DestinationId = Long.parseLong(parts[5]);
	  tempVar.RelationshipGroup = Integer.parseInt(parts[6]);
	  tempVar.TypeId = Long.parseLong(parts[7]);
	  tempVar.CharacteristicTypeId = Long.parseLong(parts[8]);
	  tempVar.ModifierId = Long.parseLong(parts[9]);
	  return tempVar;
	}

	/** 
	 Override ToString method.
	 
	 @return 
	*/
	@Override
	public String toString()
	{
	  return String.format("RF2Relationship [%1$s] Active: %2$s TypeId:%3$s Source:%4$s Destination:%5$s", Id, Active, TypeId, SourceId, DestinationId);
	}
  }