package Parser;

import java.util.*;
import java.util.Map.Entry;

/**
 * <p>Parser for Snomed rf2 files.</p>
 * @author Travis Lukach
 */
public class RF2Parser {
	/**
	 * <p>Snomed concept id for IsA relationship concept.</p>
	 */
	public static final long isAConceptId = 116680003;

	/**
	 * Snomed concept id for Synonym type concept
	 */
	public static final long synonymTypeId = 900000000000013009L;

	/**
	 * Snomed concept id for Fully Specified Name Concept
	 */
	public static final long fullySpecifiedNameConceptId = 900000000000003001L;

	/**
	 * Snomed concept id for IsA relationship concept.
	 */
	public static final String isAConceptIds = "116680003";

	/**
	 * Snomed concept id for Synonym type concept
	 */
	public static final String synonymTypeIds = "900000000000013009";

	/**
	 * Snomed concept id for Fully Specified Name Concept
	 */
	public static final String fullySpecifiedNameConceptIds = "900000000000003001";

	/**
	 * Top level Snomed concept.
	 */
	private RF2ConceptGroup rootConcept;

	/**
	 * <p>
	 * Dictionary of relationships. There may be more than one relationship per
	 * id (its a list). If so, the relationships are ordered by their effective
	 * time (later times first) so [0] is always current.
	 * </p>
	 */
	public HashMap<Long, RF2RelationshipGroup> relationshipGroups = new HashMap<Long, RF2RelationshipGroup>();

	/**
	 * <p>
	 * Dictionary of descriptions. There may be more than one description per id
	 * (its a list). If so, the descriptions are ordered by their effective time
	 * (later times first) so [0] is always current.
	 * </p>
	 */
	public HashMap<Long, RF2DescriptionGroup> descriptionGroups = new HashMap<Long, RF2DescriptionGroup>();

	/**
	 * <p>
	 * Dictionary of concepts. There may be more than one concept per id (its a
	 * list). If so, the concepts are ordered by their effective time (later
	 * times first) so [0] is always current.
	 * </p>
	 */
	public HashMap<Long, RF2ConceptGroup> conceptGroups = new HashMap<Long, RF2ConceptGroup>();

	/**
	 * <p>
	 * Dictionary of description concepts (i.e. Fully SpecifiedName, Synonymn,
	 * etc).
	 * </p>
	 */
	public HashMap<Long, RF2ConceptGroup> descriptionTypes = new HashMap<Long, RF2ConceptGroup>();

	/**
	 * Sets <i>rootConcept</i>.
	 * @return
	 */
	public RF2ConceptGroup getRootConcept() {
		return rootConcept;
	}

	/**
	 * 
	 * @param rootConcept
	 */
	public void setRootConcept(RF2ConceptGroup rootConcept) {
		this.rootConcept = rootConcept;
	}

	/**
	 * <p>
	 * Constructor.
	 * </p>
	 */
	public RF2Parser() {
	}

	/**
	 * <p>
	 * Load Snomed data from rf2 files.
	 * </p>
	 * 
	 * @param conceptPath
	 * @param relationshipPath
	 * @param descriptionPath
	 */
	public final void load(String conceptPath, String relationshipPath,
			String descriptionPath) {
		this.loadFromRF2Files(conceptPath, relationshipPath, descriptionPath);
	}

	/**
	 * <p>
	 * Load raw snomed description data into memory.
	 * </p>
	 * 
	 * @param conceptPath
	 * @param relationshipPath
	 * @param descriptionPath
	 */
	public final void loadFromRF2Files(String conceptPath,
			String relationshipPath, String descriptionPath) {
		// Load main three snomed files (concepts, relationships, and
		// descriptions).
		// Load files concurrently

		this.loadConcepts(conceptPath);
		this.loadRelationships(relationshipPath);
		this.loadDescriptions(descriptionPath);
		this.fixConcepts();
		this.fixRelationships();
		this.fixDescriptions();

		this.setRootConcept(this.findSnomedConcept("SNOMED CT CONCEPT"));
	}

	/**
	 * <p>
	 * Load raw snomed description data into memory.
	 * </p>
	 * 
	 * @param path
	 */
	private void loadRelationships(String path) {
		FileReader reader = new FileReader();
		reader.openFile(path);
		String[] parts = reader.readLine().split("[\\t]", -1);
		if ((parts.length != 10) || (parts[0].compareTo("id") != 0)
				|| (parts[1].compareTo("effectiveTime") != 0)
				|| (parts[2].compareTo("active") != 0)
				|| (parts[3].compareTo("moduleId") != 0)
				|| (parts[4].compareTo("sourceId") != 0)
				|| (parts[5].compareTo("destinationId") != 0)
				|| (parts[6].compareTo("relationshipGroup") != 0)
				|| (parts[7].compareTo("typeId") != 0)
				|| (parts[8].compareTo("characteristicTypeId") != 0)
				|| (parts[9].compareTo("modifierId") != 0)) {
			throw new RuntimeException(
					"Invalid header line to Relationship file");
		}

		RF2RelationshipGroup relationshipGroup = null;
		while (true) {
			String line = reader.readLine();
			if (line == null) {
				break;
			}

			RF2Relationship relationship = RF2Relationship.Parse(this, line);
			if (relationship != null) {
				/*
				 * Relationships in a common group are usually grouped together,
				 * so check last relationship to see if it is same group first.
				 */
				if ((relationshipGroup != null)
						&& (relationshipGroup.getId() != relationship.getId())) {
					relationshipGroup = null;
				}

				if ((relationshipGroup == null) && (!this.relationshipGroups
						.containsKey(relationship.getId()))) {
					relationshipGroup = null;
				} else {
					relationshipGroup = this.relationshipGroups
							.get(relationship.getId());
				}

				if (relationshipGroup == null) {
					relationshipGroup = new RF2RelationshipGroup();
					relationshipGroup.setParser(this);
					relationshipGroup.setId(relationship.getId());
					if (this.relationshipGroups.containsKey(relationship.getId())) {
						throw new RuntimeException(
								"Error adding relationship to dictionary");
					}
					this.relationshipGroups.put(relationship.getId(),
							relationshipGroup);
				}
				relationshipGroup.addRelationship(relationship);
			}
		}
	}

	/**
	 * <p>
	 * Pathc relationships. This must be run after concepts are read in.
	 * </p>
	 * 
	 * @param path
	 */
	private void fixRelationships() {
		for (Entry<Long, RF2RelationshipGroup> kvp : this.relationshipGroups
				.entrySet()) {
			RF2RelationshipGroup relationshipGroup = kvp.getValue();

			RF2Relationship relationship = relationshipGroup.getActive();
			if (relationship != null) {
				RF2ConceptGroup sourceConcept = this
						.getConceptGroup(relationship.getSourceId());
				RF2ConceptGroup destinationConcept = this
						.getConceptGroup(relationship.getDestinationId());

				sourceConcept.addSourceRelationship(relationshipGroup);
				destinationConcept
						.addDestinationRelationship(relationshipGroup);
			}
		}
	}

	/**
	 * <p>
	 * Return RelationshipGroup with indicated id.
	 * </p>
	 * 
	 * @param relationshipId
	 * @return
	 */
	public final RF2RelationshipGroup getRelationshipGroup(
			long relationshipId) {
		RF2RelationshipGroup relationshipGroup = null;

		if (!this.relationshipGroups.containsKey(relationshipId)) {
			throw new RuntimeException(
					String.format("Relationship %1$s not found in dictionary",
							relationshipId));
		} else {
			relationshipGroup = this.relationshipGroups.get(relationshipId);
		}
		return relationshipGroup;
	}

	/**
	 * <p>
	 * Return ConceptGroup with indicated id.
	 * </p>
	 * 
	 * @param conceptId
	 * @return
	 */
	public final RF2ConceptGroup getConceptGroup(long conceptId) {
		RF2ConceptGroup conceptGroup = null;

		if (!this.conceptGroups.containsKey(conceptId)) {
			throw new RuntimeException(String
					.format("Concept %1$s not found in dictionary", conceptId));
		} else {
			conceptGroup = this.conceptGroups.get(conceptId);
		}
		return conceptGroup;
	}

	/**
	 * <p>
	 * Return DescriptionGroup with indicated id.
	 * </p>
	 * 
	 * @param descriptionId
	 * @return
	 */
	public final RF2DescriptionGroup getDescriptionGroup(long descriptionId) {
		RF2DescriptionGroup descriptionGroup;
		if (this.descriptionGroups.containsKey(descriptionId)) {
			descriptionGroup = this.descriptionGroups.get(descriptionId);
		} else {
			throw new RuntimeException(String.format(
					"Description %1$s not found in dictionary", descriptionId));
		}
		return descriptionGroup;
	}

	/**
	 * <p>
	 * Load raw snomed description data into memory.
	 * </p>
	 * 
	 * @param path
	 */
	private void loadDescriptions(String path) {
		FileReader reader = new FileReader();
		reader.openFile(path);
		String[] parts = reader.readLine().split("[\\t]", -1);
		if ((parts.length != 9) || (parts[0].compareTo("id") != 0)
				|| (parts[1].compareTo("effectiveTime") != 0)
				|| (parts[2].compareTo("active") != 0)
				|| (parts[3].compareTo("moduleId") != 0)
				|| (parts[4].compareTo("conceptId") != 0)
				|| (parts[5].compareTo("languageCode") != 0)
				|| (parts[6].compareTo("typeId") != 0)
				|| (parts[7].compareTo("term") != 0)
				|| (parts[8].compareTo("caseSignificanceId") != 0)) {
			throw new RuntimeException(
					"Invalid header line to description file");
		}

		RF2DescriptionGroup descriptionGroup = null;
		while (true) {
			String line = reader.readLine();
			if (line == null) {
				break;
			}
			RF2Description description = RF2Description.parse(this, line);
			if (description != null) {
				/*
				 * Relationships in a common group are usually grouped together,
				 * so check last relationship to see if it is same group first.
				 */
				if ((descriptionGroup != null)
						&& (descriptionGroup.getId() != description.getId())) {
					descriptionGroup = null;
				}

				if ((descriptionGroup == null) && (!this.descriptionGroups
						.containsKey(description.getId()))) {
					descriptionGroup = null;
				} else {
					descriptionGroup = this.descriptionGroups
							.get(description.getId());
				}

				if (descriptionGroup == null) {
					descriptionGroup = new RF2DescriptionGroup();
					descriptionGroup.setParser(this);
					descriptionGroup.setId(description.getId());
					if (this.descriptionGroups.containsKey(description.getId())) {
						throw new RuntimeException(
								"Error adding description to dictionary");
					} else {
						this.descriptionGroups.put(description.getId(),
								descriptionGroup);
					}

				}
				descriptionGroup.addDescription(description);
			}
		}
		FileReader.closeFile();
	}

	/**
	 * <p>
	 * Load raw snomed description data into memory.
	 * </p>
	 * 
	 * @param path
	 */
	private void fixDescriptions() {
		for (Entry<Long, RF2DescriptionGroup> kvp : this.descriptionGroups
				.entrySet()) {
			RF2DescriptionGroup descriptionGroup = kvp.getValue();
			this.fixDescription(descriptionGroup);
		}
	}

	/**
	 * <p>
	 * Load raw snomed description data into memory.
	 * </p>
	 * 
	 * @param path
	 */
	private void fixDescription(RF2DescriptionGroup descriptionGroup) {
		RF2Description description = descriptionGroup.getActive();
		if (description == null) {
			return;
		}
		if (!this.conceptGroups.containsKey(description.getConceptId())) {
			throw new RuntimeException(
					String.format("Concept %1$s in Description %2$s not found",
							description.getConceptId(), description.getId()));
		} else {
			RF2ConceptGroup typeConcept = null;
			if (!this.descriptionTypes.containsKey(description.getTypeId())) {

				if (!this.conceptGroups.containsKey(description.getTypeId())) {
					throw new RuntimeException(String.format(
							"Description Type concept %1$s not found",
							description.getTypeId()));
				} else {
					typeConcept = this.conceptGroups.get(description.getTypeId());
				}
				if (this.descriptionTypes.containsKey(description.getTypeId())) {
					throw new RuntimeException(
							String.format("Error adding %1$s to disctionary",
									description.getTypeId()));
				} else {
					this.descriptionTypes.put(description.getId(), typeConcept);
				}
			} else {
				typeConcept = this.descriptionTypes.get(description.getTypeId());
			}
			this.getConceptGroup(description.getConceptId())
					.addDescriptionGroup(descriptionGroup);
		}
	}

	/**
	 * <p>
	 * Load raw snomed concept data into memory.
	 * </p>
	 * 
	 * @param path
	 */
	private void loadConcepts(String path) {
		FileReader reader = new FileReader();
		reader.openFile(path);
		String[] parts = reader.readLine().split("[\\t]");
		if ((parts.length != 5) || (parts[0].compareTo("id") != 0)
				|| (parts[1].compareTo("effectiveTime") != 0)
				|| (parts[2].compareTo("active") != 0)
				|| (parts[3].compareTo("moduleId") != 0)
				|| (parts[4].compareTo("definitionStatusId") != 0)) {
			throw new RuntimeException("Invalid header line to concept file");
		}

		RF2ConceptGroup conceptGroup = null;
		while (true) {
			String line = reader.readLine();
			if (line == null) {
				break;
			}
			RF2Concept concept = RF2Concept.parse(this, line);
			if (concept != null) {
				/*
				 * Relationships in a common group are usually grouped together,
				 * so check last relationship to see if it is same group first.
				 */
				if ((conceptGroup != null) && (conceptGroup.getId() != concept.getId())) {
					conceptGroup = null;
				}
				if ((conceptGroup == null)
						&& (!this.conceptGroups.containsKey(concept.getId()))) {
					conceptGroup = this.conceptGroups.get(concept.getId());
					conceptGroup = null;
				} else {
					conceptGroup = this.conceptGroups.get(concept.getId());
				}

				if (conceptGroup == null) {
					conceptGroup = new RF2ConceptGroup();
					conceptGroup.setParser(this);
					conceptGroup.setId(concept.getId());

					if (this.conceptGroups.containsKey(concept.getId())) {
						throw new RuntimeException(
								"Error adding concept to dictionary");
					}
					{
						this.conceptGroups.put(conceptGroup.getId(), conceptGroup);
					}

				}
				conceptGroup.addConcept(concept);
			}
		}
		FileReader.closeFile();
	}

	/**
	 * <p>
	 * Load raw snomed concept data into memory.
	 * </p>
	 * 
	 * @param path
	 */
	private void fixConcepts() {
	}

	/**
	 * <p>
	 * Find snomed concept with indicated name.
	 * </p>
	 * 
	 * @param name
	 * @return
	 */
	public final RF2ConceptGroup findSnomedConcept(String name) {
		name = name.toUpperCase();

		ArrayList<RF2ConceptGroup> retVal = new ArrayList<RF2ConceptGroup>();
		for (RF2DescriptionGroup descriptionGroup : this.descriptionGroups
				.values()) {
			for (RF2Description description : descriptionGroup.getItems()) {
				if (description == null
						? null
						: description.getTerm().trim().toUpperCase()
								.compareTo(name) == 0) {
					RF2ConceptGroup cg = this
							.getConceptGroup(description.getConceptId());
					if (retVal.contains(cg) == false) {
						retVal.add(cg);
					}
				}
			}
		}

		if (retVal.isEmpty()) {
			throw new RuntimeException("No Snomed CT Concept records found");
		}
		if (retVal.size() > 1) {
			throw new RuntimeException(
					"Multiple Snomed CT Concept records found");
		}

		return retVal.get(0);
	}


}