package Parser;

import java.util.*;

/**
 * Group of concepts, all with same SNOMED id.
 */
public class RF2ConceptGroup extends TRF2ItemGroup<RF2Concept> {
	/**
	 * List of all snomed descriptions for this concept.
	 */
	private ArrayList<Long> descriptionGroupIds = new ArrayList<Long>();

	/**
	 * List of all snomed attribute concepts in which this concept is the source
	 * concept.
	 */
	private ArrayList<Long> sourceRelationshipIds = new ArrayList<Long>();

	/**
	 * List of all snomed attribute concepts in which this concept is the
	 * destination concept.
	 */
	private ArrayList<Long> destinationRelationshipIds = new ArrayList<Long>();

	/**
	 * Chain of is a parents.
	 */
	public ArrayList<Long> IsAParents = new ArrayList<Long>();

	/**
	 * Concept id for the class that this snomed is an instance of.
	 */
	public long ClassConceptId = -1;

	/**
	 * Top level node that this concept group is descended from.
	 */
	public long TopLevelConceptId = -1;

	/**
	 * Lazy load Description groups.
	 */
	public final RF2DescriptionGroup[] getDescriptionGroups() {
		// # Tested.
		if (this.descriptionGroups == null) {
			RF2DescriptionGroup[] temp = new RF2DescriptionGroup[this.descriptionGroupIds.size()];
			for (int i = 0; i < this.descriptionGroupIds.size(); i++) {
				temp[i] = this.Parser.GetDescriptionGroup(this.descriptionGroupIds.get(i));
			}
			this.descriptionGroups = temp;
		}
		return this.descriptionGroups;
	}

	/**
	 * Description groups
	 */
	private RF2DescriptionGroup[] descriptionGroups = null;

	/**
	 * Lazy load Source relationship groups.
	 */
	public final RF2RelationshipGroup[] getSourceRelationships() {
		// # Not Tested.
		if (this.sourceRelationshipGroups == null) {
			RF2RelationshipGroup[] temp = new RF2RelationshipGroup[this.sourceRelationshipIds.size()];
			for (int i = 0; i < this.sourceRelationshipIds.size(); i++) {
				temp[i] = this.Parser.GetRelationshipGroup(this.sourceRelationshipIds.get(i));
			}
			this.sourceRelationshipGroups = temp;
		}
		return this.sourceRelationshipGroups;
	}

	/**
	 * Relationship groups in which this concept is the source.
	 */
	private RF2RelationshipGroup[] sourceRelationshipGroups = null;

	/**
	 * Lazy load Destination relationship groups.
	 */
	public final RF2RelationshipGroup[] getDestinationRelationships() {
		// # Tested.
		if (this.destinationRelationshipGroups == null) {
			RF2RelationshipGroup[] temp = new RF2RelationshipGroup[this.destinationRelationshipIds.size()];
			for (int i = 0; i < this.destinationRelationshipIds.size(); i++) {
				temp[i] = this.Parser.GetRelationshipGroup(this.destinationRelationshipIds.get(i));
			}
			this.destinationRelationshipGroups = temp;
		}
		return this.destinationRelationshipGroups;
	}

	/**
	 * Relationship groups in which this concept is the destination.
	 */
	private RF2RelationshipGroup[] destinationRelationshipGroups = null;

	/**
	 * Constructor
	 */
	public RF2ConceptGroup() {
	}

	/**
	 * Add concept to concept group.
	 * 
	 * @param concept
	 */
	public final void AddConcept(RF2Concept concept) {
		this.items.add(concept);
	}

	/**
	 * Add description group to concept group.
	 * 
	 * @param descriptionGroup
	 */
	public final void AddDescriptionGroup(RF2DescriptionGroup descriptionGroup) {
		if (this.descriptionGroupIds.contains(descriptionGroup.Id) == true) {
			return;
		}
		this.descriptionGroupIds.add(descriptionGroup.Id);
		// force lazy reloading of description groups id called.
		this.descriptionGroups = null;
	}

	/**
	 * Add a source relationship to concept group.
	 * 
	 * @param relationshipGroup
	 *            Relationship in which this instance is the source
	 */
	public final void AddSourceRelationship(RF2RelationshipGroup relationshipGroup) {
		if (this.sourceRelationshipIds.contains(relationshipGroup.Id) == true) {
			return;
		}
		this.sourceRelationshipIds.add(relationshipGroup.Id);

		// force lazy reloading of source relationship groups id called.
		this.sourceRelationshipGroups = null;
	}

	/**
	 * Add a destination relationship to concept group.
	 * 
	 * @param relationshipGroup
	 *            Relationship in which this instance is the destination
	 */
	public final void AddDestinationRelationship(RF2RelationshipGroup relationshipGroup) {
		if (this.destinationRelationshipIds.contains(relationshipGroup.Id) == true) {
			return;
		}
		this.destinationRelationshipIds.add(relationshipGroup.Id);

		// force lazy reloading of source relationship groups id called.
		this.destinationRelationshipGroups = null;
	}

	/**
	 * Return Snomed fully qualified name.
	 * 
	 * @return
	 */
	public final String GetFullySpecifiedName() {
		String fsnAny = "";

		for (RF2DescriptionGroup dg : this.getDescriptionGroups()) {
			for (RF2Description description : dg.getItems()) {
				if (description.TypeId == RF2Parser.FullySpecifiedNameConceptId) {
					if (description.LanguageCode.equals("en")) {
						return description.Term;
					}
					fsnAny = description.Term;
				}
			}
		}

		return fsnAny;
	}

	/**
	 * Return list of all SNOMED relationship groups that are IsA relationships,
	 * where this concept is the source.
	 * 
	 * @return
	 */
	public final ArrayList<RF2RelationshipGroup> IsAChildRelationships() {
		ArrayList<RF2RelationshipGroup> retVal = new ArrayList<RF2RelationshipGroup>();

		for (RF2RelationshipGroup g : this.getDestinationRelationships()) {
			RF2Relationship relationship = g.getActive();
			if ((relationship.Active == true) && (relationship.TypeId == RF2Parser.IsAConceptId)) {
				retVal.add(g);
			}
		}
		return retVal;
	}

	/**
	 * Override ToString method.
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return String.format("RF2ConceptGroup [%1$s] %2$s", Id, GetFullySpecifiedName());
	}
}