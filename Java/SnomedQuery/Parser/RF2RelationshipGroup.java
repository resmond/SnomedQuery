package Parser;

  /** 
   Group of relationships, all with same SNOMED id.
  */
  public class RF2RelationshipGroup extends TRF2ItemGroup<RF2Relationship>
  {
	/** 
	 Constructor
	*/
	public RF2RelationshipGroup()
	{
	}

	/** 
	 Add relationsip to relationship group.
	 
	 @param relationship
	*/
	public final void AddRelationship(RF2Relationship relationship)
	{
	  this.items.add(relationship);
	}

	/** 
	 Override ToString() method.
	 
	 @return 
	*/
	@Override
	public String toString()
	{
	  StringBuilder sb = new StringBuilder();
	  sb.append(String.format("RF2RelationshipGroup [%1$s] ", Id));
	  RF2Relationship active = this.items.get(0);
	  sb.append(String.format("Type [%1$s] '%2$s'", active.TypeId, active.getTypeConcept().GetFullySpecifiedName()));
	  sb.append(String.format("Source [%1$s] '%2$s'", active.SourceId, active.getSource().GetFullySpecifiedName()));
	  sb.append(String.format("Dest [%1$s] '%2$s'", active.DestinationId, active.getDestination().GetFullySpecifiedName()));

	  return sb.toString();
	}
  }