package Parser;

/**
 * <p>
 * Stored data from one line of the RFS concept file.
 * </p>
 * 
 * @author Travis Lukach
 */
public class RF2Relationship extends RF2ItemSingle {
	/**
	 * <p>
	 * Snomed Id of relationship source
	 * </p>
	 */
	private long sourceId;

	/**
	 * <p>
	 * Snomed Id of relationship destination
	 * </p>
	 */
	private long destinationId;

	/**
	 * <p>
	 * Snomed relationship group.
	 * </p>
	 */
	private int relationshipGroup;

	/**
	 * <p>
	 * Snomed relationship type.
	 * </p>
	 */
	private long typeId;

	/**
	 * <p>
	 * Snomed relationship characteristic type id.
	 * </p>
	 */
	private long characteristicTypeId;

	/**
	 * <p>
	 * Snomed modifier id.
	 * </p>
	 */
	private long modifierId;

	/**
	 * <p>
	 * Gets <i>sourceId</i>.
	 * </p>
	 * 
	 * @return
	 */
	public long getSourceId() {
		return sourceId;
	}

	/**
	 * <p>
	 * Sets <i>sourceId</i>.
	 * </p>
	 * 
	 * @param sourceId
	 */
	public void setSourceId(long sourceId) {
		this.sourceId = sourceId;
	}

	/**
	 * <p>
	 * Gets <i>destinationId</i>.
	 * </p>
	 * 
	 * @return
	 */
	public long getDestinationId() {
		return destinationId;
	}

	/**
	 * <p>
	 * Sets <i>destinationId</i>.
	 * </p>
	 * 
	 * @param destinationId
	 */
	public void setDestinationId(long destinationId) {
		this.destinationId = destinationId;
	}

	/**
	 * <p>
	 * Gets <i>relationshipGroup</i>.
	 * </p>
	 * 
	 * @return
	 */
	public int getRelationshipGroup() {
		return relationshipGroup;
	}

	/**
	 * <p>
	 * Sets <i>relationshipGroup</i>.
	 * </p>
	 * 
	 * @param relationshipGroup
	 */
	public void setRelationshipGroup(int relationshipGroup) {
		this.relationshipGroup = relationshipGroup;
	}

	/**
	 * <p>
	 * Gets <i>typeId</i>.
	 * </p>
	 * 
	 * @return
	 */
	public long getTypeId() {
		return typeId;
	}

	/**
	 * <p>
	 * Sets <i>typeId</i>.
	 * </p>
	 * 
	 * @param typeId
	 */
	public void setTypeId(long typeId) {
		this.typeId = typeId;
	}

	/**
	 * <p>
	 * Gets <i>characteristicTypeId</i>.
	 * </p>
	 * 
	 * @return
	 */
	public long getCharacteristicTypeId() {
		return characteristicTypeId;
	}

	/**
	 * <p>
	 * Sets <i>characteristicTypeId</i>.
	 * </p>
	 * 
	 * @param characteristicTypeId
	 */
	public void setCharacteristicTypeId(long characteristicTypeId) {
		this.characteristicTypeId = characteristicTypeId;
	}

	/**
	 * <p>
	 * Gets <i>modifierId</i>.
	 * </p>
	 * 
	 * @return
	 */
	public long getModifierId() {
		return modifierId;
	}

	/**
	 * <p>
	 * Sets <i>modifierId</i>.
	 * </p>
	 * 
	 * @param modifierId
	 */
	public void setModifierId(long modifierId) {
		this.modifierId = modifierId;
	}

	/**
	 * <p>
	 * Lazy load TypeConcept.
	 * </p>
	 * 
	 * @return
	 */
	public final RF2ConceptGroup getTypeConcept() {
		if (this.typeConcept == null) {
			this.typeConcept = this.getParser().getConceptGroup(this.typeId);
		}
		return this.typeConcept;
	}

	/**
	 * <p>
	 * Backing field for TypeConcept property.
	 * </p>
	 */
	private RF2ConceptGroup typeConcept = null;

	/**
	 * <p>
	 * Lazy load Source.
	 * </p>
	 * 
	 * @return
	 */
	public final RF2ConceptGroup getSource() {
		if (this.source == null) {
			this.source = this.getParser().getConceptGroup(this.sourceId);
		}
		return this.source;
	}

	/**
	 * <p>
	 * Backing field for Source property.
	 * </p>
	 */
	private RF2ConceptGroup source = null;

	/**
	 * <p>
	 * Lazy load Destination.
	 * </p>
	 * 
	 * @return
	 */
	public final RF2ConceptGroup getDestination() {
		if (this.destination == null) {
			this.destination = this.getParser()
					.getConceptGroup(this.destinationId);
		}
		return this.destination;
	}

	/**
	 * <p>
	 * Backing field for Destination property.
	 * </p>
	 */
	public RF2ConceptGroup destination = null;

	/**
	 * <p>
	 * Constructor
	 * </p>
	 */
	public RF2Relationship() {
	}

	/**
	 * <p>
	 * Parse single line from relationship rf2 file.
	 * </p>
	 * 
	 * @param parser
	 * @param line
	 * @return
	 */
	public static RF2Relationship Parse(RF2Parser parser, String line) {
		String[] parts = line.split("[\\t]", -1);
		if (parts.length != 10) {
			throw new RuntimeException("Invalid Relationship line");
		}

		RF2Relationship tempVar = new RF2Relationship();
		tempVar.setParser(parser);
		tempVar.setId(Long.parseLong(parts[0]));
		tempVar.setEffectiveTime(parseEffectiveTime(parts[1]));
		tempVar.setActive(parseBool(parts[2]));
		tempVar.setModuleId(Long.parseLong(parts[3]));
		tempVar.sourceId = Long.parseLong(parts[4]);
		tempVar.destinationId = Long.parseLong(parts[5]);
		tempVar.setRelationshipGroup(Integer.parseInt(parts[6]));
		tempVar.typeId = Long.parseLong(parts[7]);
		tempVar.setCharacteristicTypeId(Long.parseLong(parts[8]));
		tempVar.setModifierId(Long.parseLong(parts[9]));
		return tempVar;
	}

	/**
	 * <p>
	 * Override ToString method.
	 * </p>
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return String.format(
				"RF2Relationship [%1$s] Active: %2$s TypeId:%3$s Source:%4$s Destination:%5$s",
				getId(), isActive(), typeId, sourceId, destinationId);
	}

}