package Parser;

import java.util.ArrayList;

/**
 * <p>
 * Group of concepts, all with same SNOMED id.
 * </p>
 * 
 * @author Travis Lukach
 */
public class RF2ConceptGroup extends TRF2ItemGroup<RF2Concept> {
	/**
	 * <p>
	 * List of all snomed descriptions for this concept.
	 * </p>
	 */
	private ArrayList<Long> descriptionGroupIds = new ArrayList<Long>();

	/**
	 * <p>
	 * List of all snomed attribute concepts in which this concept is the source
	 * concept.
	 * </p>
	 */
	private ArrayList<Long> sourceRelationshipIds = new ArrayList<Long>();

	/**
	 * <p>
	 * List of all snomed attribute concepts in which this concept is the
	 * destination concept.
	 * </p>
	 */
	private ArrayList<Long> destinationRelationshipIds = new ArrayList<Long>();

	/**
	 * <p>
	 * Chain of is a parents.
	 * </p>
	 */
	private ArrayList<Long> isAParents = new ArrayList<Long>();

	/**
	 * <p>
	 * Concept id for the class that this snomed is an instance of.
	 * </p>
	 */
	private long classConceptId = -1;

	/**
	 * <p>
	 * Top level node that this concept group is descended from.
	 * </p>
	 */
	private long topLevelConceptId = -1;

	/**
	 * <p>
	 * Gets <i>topLevelConceptId</i>.
	 * </p>
	 * 
	 * @return
	 */
	public long getTopLevelConceptId() {
		return topLevelConceptId;
	}

	/**
	 * <p>
	 * Sets <i>topLevelConceptId</i>.
	 * </p>
	 * 
	 * @param topLevelConceptId
	 */
	public void setTopLevelConceptId(long topLevelConceptId) {
		this.topLevelConceptId = topLevelConceptId;
	}

	/**
	 * <p>
	 * Gets <i>classConceptId</i>.
	 * </p>
	 * 
	 * @return
	 */
	public long getClassConceptId() {
		return classConceptId;
	}
	/**
	 * <p>
	 * Sets <i>classConceptId</i>.
	 * </p>
	 * 
	 * @param classConceptId
	 */
	public void setClassConceptId(long classConceptId) {
		this.classConceptId = classConceptId;
	}
	/**
	 * Gets <i>isAParents</i>.
	 * 
	 * @return
	 */
	public ArrayList<Long> getIsAParents() {
		return isAParents;
	}
	/**
	 * <p>
	 * Sets <i>isAParents</i>.
	 * </p>
	 * 
	 * @param isAParents
	 */
	public void setIsAParents(ArrayList<Long> isAParents) {
		this.isAParents = isAParents;
	}

	/**
	 * <p>
	 * Lazy load Description groups.
	 * </p>
	 * 
	 * @return
	 */
	public final RF2DescriptionGroup[] getDescriptionGroups() {
		// # Tested.
		if (this.descriptionGroups == null) {
			RF2DescriptionGroup[] temp = new RF2DescriptionGroup[this.descriptionGroupIds
					.size()];
			for (int i = 0; i < this.descriptionGroupIds.size(); i++) {
				temp[i] = this.getParser()
						.getDescriptionGroup(this.descriptionGroupIds.get(i));
			}
			this.descriptionGroups = temp;
		}
		return this.descriptionGroups;
	}

	/**
	 * <p>
	 * Description groups
	 * </p>
	 */
	private RF2DescriptionGroup[] descriptionGroups = null;

	/**
	 * <p>
	 * Lazy load Source relationship groups.
	 * </p>
	 */
	public final RF2RelationshipGroup[] getSourceRelationships() {
		// # Not Tested.
		if (this.sourceRelationshipGroups == null) {
			RF2RelationshipGroup[] temp = new RF2RelationshipGroup[this.sourceRelationshipIds
					.size()];
			for (int i = 0; i < this.sourceRelationshipIds.size(); i++) {
				temp[i] = this.getParser().getRelationshipGroup(
						this.sourceRelationshipIds.get(i));
			}
			this.sourceRelationshipGroups = temp;
		}
		return this.sourceRelationshipGroups;
	}

	/**
	 * <p>
	 * Relationship groups in which this concept is the source.
	 * </p>
	 */
	private RF2RelationshipGroup[] sourceRelationshipGroups = null;

	/**
	 * <p>
	 * Lazy load Destination relationship groups.
	 * </p>
	 * 
	 * @return
	 */
	public final RF2RelationshipGroup[] getDestinationRelationships() {
		// # Tested.
		if (this.destinationRelationshipGroups == null) {
			RF2RelationshipGroup[] temp = new RF2RelationshipGroup[this.destinationRelationshipIds
					.size()];
			for (int i = 0; i < this.destinationRelationshipIds.size(); i++) {
				temp[i] = this.getParser().getRelationshipGroup(
						this.destinationRelationshipIds.get(i));
			}
			this.destinationRelationshipGroups = temp;
		}
		return this.destinationRelationshipGroups;
	}

	/**
	 * <p>
	 * Relationship groups in which this concept is the destination.
	 * </p>
	 */
	private RF2RelationshipGroup[] destinationRelationshipGroups = null;

	/**
	 * <p>
	 * Constructor.
	 * </p>
	 */
	public RF2ConceptGroup() {
	}

	/**
	 * <p>
	 * Add concept to concept group.
	 * </p>
	 * 
	 * @param concept
	 */
	public final void addConcept(final RF2Concept concept) {
		this.items.add(concept);
	}

	/**
	 * <p>
	 * Add description group to concept group.
	 * </p>
	 * 
	 * @param descriptionGroup
	 */
	public final void addDescriptionGroup(
			final RF2DescriptionGroup descriptionGroup) {
		if (this.descriptionGroupIds.contains(descriptionGroup.getId()) == true) {
			return;
		}
		this.descriptionGroupIds.add(descriptionGroup.getId());
		// force lazy reloading of description groups id called.
		this.descriptionGroups = null;
	}

	/**
	 * <p>
	 * Add a source relationship to concept group.
	 * </p>
	 * 
	 * @param relationshipGroup
	 *            Relationship in which this instance is the source
	 */
	public final void addSourceRelationship(
			final RF2RelationshipGroup relationshipGroup) {
		if (this.sourceRelationshipIds.contains(relationshipGroup.getId()) == true) {
			return;
		}
		this.sourceRelationshipIds.add(relationshipGroup.getId());

		// force lazy reloading of source relationship groups id called.
		this.sourceRelationshipGroups = null;
	}

	/**
	 * Add a destination relationship to concept group.
	 * 
	 * @param relationshipGroup
	 *            Relationship in which this instance is the destination
	 */
	public final void addDestinationRelationship(
			final RF2RelationshipGroup relationshipGroup) {
		if (this.destinationRelationshipIds
				.contains(relationshipGroup.getId()) == true) {
			return;
		}
		this.destinationRelationshipIds.add(relationshipGroup.getId());

		// force lazy reloading of source relationship groups id called.
		this.destinationRelationshipGroups = null;
	}

	/**
	 * Return Snomed fully qualified name.
	 * 
	 * @return
	 */
	public final String getFullySpecifiedName() {
		String fsnAny = "";

		for (RF2DescriptionGroup dg : this.getDescriptionGroups()) {
			for (RF2Description description : dg.getItems()) {
				if (description.getTypeId() == RF2Parser.fullySpecifiedNameConceptId) {
					if (description.getLanguageCode().equals("en")) {
						return description.getTerm();
					}
					fsnAny = description.getTerm();
				}
			}
		}

		return fsnAny;
	}

	/**
	 * <p>
	 * Return list of all SNOMED relationship groups that are IsA relationships,
	 * where this concept is the source.
	 * </p>
	 * 
	 * @return ArrayList of Relationship groups.
	 */
	public final ArrayList<RF2RelationshipGroup> isAChildRelationships() {
		ArrayList<RF2RelationshipGroup> retVal = new ArrayList<RF2RelationshipGroup>();

		for (RF2RelationshipGroup g : this.getDestinationRelationships()) {
			RF2Relationship relationship = g.getActive();
			if ((relationship.isActive() == true)
					&& (relationship.getTypeId() == RF2Parser.isAConceptId)) {
				retVal.add(g);
			}
		}
		return retVal;
	}

	/**
	 * <p>
	 * Override ToString method.
	 * </p>
	 * 
	 * @return
	 */
	@Override
	public final String toString() {
		return String.format("RF2ConceptGroup [%1$s] %2$s", getId(),
				getFullySpecifiedName());
	}

}