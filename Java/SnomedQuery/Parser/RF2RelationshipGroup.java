package Parser;

/**
 * <p>
 * Group of relationships, all with same SNOMED id.
 * </p>
 * 
 * @author Travis Lukach
 */
public class RF2RelationshipGroup extends TRF2ItemGroup<RF2Relationship> {
	/**
	 * <p>
	 * Constructor
	 * </p>
	 */
	public RF2RelationshipGroup() {
	}

	/**
	 * <p>
	 * Add relationsip to relationship group.
	 * </p>
	 * 
	 * @param relationship
	 */
	public final void addRelationship(RF2Relationship relationship) {
		this.items.add(relationship);
	}

	/**
	 * <p>
	 * Override ToString() method.
	 * </p>
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("RF2RelationshipGroup [%1$s] ", getId()));
		RF2Relationship active = this.items.get(0);
		sb.append(String.format("Type [%1$s] '%2$s'", active.getTypeId(),
				active.getTypeConcept().getFullySpecifiedName()));
		sb.append(String.format("Source [%1$s] '%2$s'", active.getSourceId(),
				active.getSource().getFullySpecifiedName()));
		sb.append(String.format("Dest [%1$s] '%2$s'", active.getDestinationId(),
				active.getDestination().getFullySpecifiedName()));

		return sb.toString();
	}
}