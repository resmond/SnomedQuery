package Parser;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Parser fro Snomed rf2 files.
 */
public class RF2Parser {
	/**
	 * Snomed concept id for IsA relationship concept.
	 */
	public static final long IsAConceptId = 116680003;

	/**
	 * Snomed concept id for Synonym type concept
	 */
	public static final long SynonymTypeId = 900000000000013009L;

	/**
	 * Snomed concept id for Fully Specified Name Concept
	 */
	public static final long FullySpecifiedNameConceptId = 900000000000003001L;

	/**
	 * Snomed concept id for IsA relationship concept.
	 */
	public static final String IsAConceptIds = "116680003";

	/**
	 * Snomed concept id for Synonym type concept
	 */
	public static final String SynonymTypeIds = "900000000000013009";

	/**
	 * Snomed concept id for Fully Specified Name Concept
	 */
	public static final String FullySpecifiedNameConceptIds = "900000000000003001";

	/**
	 * Top level Snomed concept.
	 */
	public RF2ConceptGroup RootConcept;

	/**
	 * Dictionary of relationships. There may be more than one relationship per
	 * id (its a list). If so, the relationships are ordered by their effective
	 * time (later times first) so [0] is always current.
	 */
	public ConcurrentHashMap<Long, RF2RelationshipGroup> RelationshipGroups = new ConcurrentHashMap<Long, RF2RelationshipGroup>();

	/**
	 * Dictionary of descriptions. There may be more than one description per id
	 * (its a list). If so, the descriptions are ordered by their effective time
	 * (later times first) so [0] is always current.
	 */
	public ConcurrentHashMap<Long, RF2DescriptionGroup> DescriptionGroups = new ConcurrentHashMap<Long, RF2DescriptionGroup>();

	/**
	 * Dictionary of concepts. There may be more than one concept per id (its a
	 * list). If so, the concepts are ordered by their effective time (later
	 * times first) so [0] is always current.
	 */
	public ConcurrentHashMap<Long, RF2ConceptGroup> ConceptGroups = new ConcurrentHashMap<Long, RF2ConceptGroup>();

	/**
	 * Dictionary of description concepts (i.e. Fully SpecifiedName, Synonymn,
	 * etc).
	 */
	public ConcurrentHashMap<Long, RF2ConceptGroup> DescriptionTypes = new ConcurrentHashMap<Long, RF2ConceptGroup>();

	/**
	 * Constructor.
	 */
	public RF2Parser() {
	}

	/**
	 * Load Snomed data from rf2 files.
	 * 
	 * @param p
	 * @param path
	 */
	public final void Load(String conceptPath, String relationshipPath, String descriptionPath) {
		this.LoadFromRF2Files(conceptPath, relationshipPath, descriptionPath);
	}

	/**
	 * Load raw snomed description data into memory.
	 * 
	 * @param path
	 */
	public final void LoadFromRF2Files(String conceptPath, String relationshipPath, String descriptionPath) {
		// Load main three snomed files (concepts, relationships, and
		// descriptions).
		// Load files concurrently
		{
//			Thread loadConceptsTask = new Thread(() -> this.LoadConcepts(conceptPath));
//			Thread loadRelationshipsTask = new Thread(() -> this.LoadRelationships(relationshipPath));
//			Thread loadDescriptionsTask = new Thread(() -> this.LoadDescriptions(descriptionPath));
			System.out.println("Parsing concepts");
			this.LoadConcepts(conceptPath);
			System.out.println("Parsing relationships");
			this.LoadRelationships(relationshipPath);
			System.out.println("Parsing descrptions");
			this.LoadDescriptions(descriptionPath);
//
//			loadConceptsTask.start();
//			loadRelationshipsTask.start();
//			loadDescriptionsTask.start();
////			try {
//				loadConceptsTask.join();
//				loadRelationshipsTask.join();
//				loadDescriptionsTask.join();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}

//		{
//			// Perform cleanup on above loaded files.
//			Thread fixConceptsTask = new Thread(this.FixConcepts());
//			Thread fixRelationshipsTask = new Thread(this.FixRelationships());
//			Thread fixDescriptionsTask = new Thread(this.FixDescriptions());
			this.FixConcepts();
			this.FixRelationships();
			this.FixDescriptions();
//
//			fixConceptsTask.start();
//			fixRelationshipsTask.start();
//			fixDescriptionsTask.start();
//
//			try {
//				fixConceptsTask.join();
//				fixRelationshipsTask.join();
//				fixDescriptionsTask.join();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}

		this.RootConcept = this.FindSnomedConcept("SNOMED CT CONCEPT");
	}

	/**
	 * Load raw snomed description data into memory.
	 * 
	 * @param path
	 */
	private void LoadRelationships(String path) {
		FileReader reader = new FileReader();
		reader.openFile(path);
		String[] parts = reader.readLine().split("[\\t]", -1);
		if ((parts.length != 10) || (parts[0].compareTo("id") != 0) || (parts[1].compareTo("effectiveTime") != 0)
				|| (parts[2].compareTo("active") != 0) || (parts[3].compareTo("moduleId") != 0)
				|| (parts[4].compareTo("sourceId") != 0) || (parts[5].compareTo("destinationId") != 0)
				|| (parts[6].compareTo("relationshipGroup") != 0) || (parts[7].compareTo("typeId") != 0)
				|| (parts[8].compareTo("characteristicTypeId") != 0) || (parts[9].compareTo("modifierId") != 0)) {
			throw new RuntimeException("Invalid header line to Relationship file");
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
				if ((relationshipGroup != null) && (relationshipGroup.Id != relationship.Id)) {
					relationshipGroup = null;
				}
				
				if ((relationshipGroup == null) && (!this.RelationshipGroups.containsKey(relationship.Id))) {
					relationshipGroup = null;
				} else {
					relationshipGroup = this.RelationshipGroups.get(relationship.Id);
				}

				if (relationshipGroup == null) {
					relationshipGroup = new RF2RelationshipGroup();
					relationshipGroup.Parser = this;
					relationshipGroup.Id = relationship.Id;
					if (this.RelationshipGroups.containsKey(relationship.Id)) {
						throw new RuntimeException("Error adding relationship to dictionary");
					}
					this.RelationshipGroups.put(relationship.Id, relationshipGroup);
				}
				relationshipGroup.AddRelationship(relationship);
			}
		}
	}

	/**
	 * Pathc relationships. This must be run after concepts are read in.
	 * 
	 * @param path
	 * @return
	 */
	private Runnable FixRelationships() {
		for (Entry<Long, RF2RelationshipGroup> kvp : this.RelationshipGroups.entrySet()) {
			RF2RelationshipGroup relationshipGroup = kvp.getValue();

			RF2Relationship relationship = relationshipGroup.getActive();
			if (relationship != null) {
				RF2ConceptGroup sourceConcept = this.GetConceptGroup(relationship.SourceId);
				RF2ConceptGroup destinationConcept = this.GetConceptGroup(relationship.DestinationId);

				sourceConcept.AddSourceRelationship(relationshipGroup);
				destinationConcept.AddDestinationRelationship(relationshipGroup);
			}
		}
		return null;
	}

	/**
	 * Return RelationshipGroup with indicated id.
	 * 
	 * @param relationshipId
	 * @return
	 */
	public final RF2RelationshipGroup GetRelationshipGroup(long relationshipId) {
		RF2RelationshipGroup relationshipGroup = null;
		
		if (!this.RelationshipGroups.containsKey(relationshipId)) {
			throw new RuntimeException(String.format("Relationship %1$s not found in dictionary", relationshipId));
		} else {
			relationshipGroup = this.RelationshipGroups.get(relationshipId);
		}
		return relationshipGroup;
	}

	/**
	 * Return ConceptGroup with indicated id.
	 * 
	 * @param conceptId
	 * @return
	 */
	public final RF2ConceptGroup GetConceptGroup(long conceptId) {
		RF2ConceptGroup conceptGroup = null;
		
		if (!this.ConceptGroups.containsKey(conceptId)) {
			throw new RuntimeException(String.format("Concept %1$s not found in dictionary", conceptId));
		} else {
			conceptGroup = this.ConceptGroups.get(conceptId);
		}
		return conceptGroup;
	}

	/**
	 * Return DescriptionGroup with indicated id.
	 * 
	 * @param descriptionId
	 * @return
	 */
	public final RF2DescriptionGroup GetDescriptionGroup(long descriptionId) {
		RF2DescriptionGroup descriptionGroup;
		if (this.DescriptionGroups.containsKey(descriptionId)) {
			descriptionGroup = this.DescriptionGroups.get(descriptionId);
		} else {
			throw new RuntimeException(String.format("Description %1$s not found in dictionary", descriptionId));
		}
		return descriptionGroup;
	}

	/**
	 * Load raw snomed description data into memory.
	 * 
	 * @param path
	 */
	private void LoadDescriptions(String path) {
		FileReader reader = new FileReader();
		reader.openFile(path);
		String[] parts = reader.readLine().split("[\\t]", -1);
		if ((parts.length != 9) || (parts[0].compareTo("id") != 0) || (parts[1].compareTo("effectiveTime") != 0)
				|| (parts[2].compareTo("active") != 0) || (parts[3].compareTo("moduleId") != 0)
				|| (parts[4].compareTo("conceptId") != 0) || (parts[5].compareTo("languageCode") != 0)
				|| (parts[6].compareTo("typeId") != 0) || (parts[7].compareTo("term") != 0)
				|| (parts[8].compareTo("caseSignificanceId") != 0)) {
			throw new RuntimeException("Invalid header line to description file");
		}

		RF2DescriptionGroup descriptionGroup = null;
		while (true) {
			String line = reader.readLine();
			if (line == null) {
				break;
			}
			RF2Description description = RF2Description.Parse(this, line);
			if (description != null) {
				/*
				 * Relationships in a common group are usually grouped together,
				 * so check last relationship to see if it is same group first.
				 */
				if ((descriptionGroup != null) && (descriptionGroup.Id != description.Id)) {
					descriptionGroup = null;
				}

				if ((descriptionGroup == null) && (!this.DescriptionGroups.containsKey(description.Id))) {
					descriptionGroup = null;
				} else {
					descriptionGroup = this.DescriptionGroups.get(description.Id);
				}

				if (descriptionGroup == null) {
					descriptionGroup = new RF2DescriptionGroup();
					descriptionGroup.Parser = this;
					descriptionGroup.Id = description.Id;
					if (this.DescriptionGroups.containsKey(description.Id)) {
						throw new RuntimeException("Error adding description to dictionary");
					}
					else 
						this.DescriptionGroups.put(description.Id, descriptionGroup);
					
				}
				descriptionGroup.AddDescription(description);
			}
		}
		FileReader.closeFile();
	}

	/**
	 * Load raw snomed description data into memory.
	 * 
	 * @param path
	 * @return
	 */
	private Runnable FixDescriptions() {
		for (Entry<Long, RF2DescriptionGroup> kvp : this.DescriptionGroups.entrySet()) {
			RF2DescriptionGroup descriptionGroup = kvp.getValue();
			this.FixDescription(descriptionGroup);
		}
		return null;
	}

	/**
	 * Load raw snomed description data into memory.
	 * 
	 * @param path
	 */
	private void FixDescription(RF2DescriptionGroup descriptionGroup) {
		RF2Description description = descriptionGroup.getActive();
		if (description == null) {
			return;
		}		
		if (!this.ConceptGroups.containsKey(description.ConceptId)) {
			throw new RuntimeException(
					String.format("Concept %1$s in Description %2$s not found", description.ConceptId, description.Id));
		} else {
			RF2ConceptGroup typeConcept = null;
			if (!this.DescriptionTypes.containsKey(description.TypeId)) {
				
				if (!this.ConceptGroups.containsKey(description.TypeId)) {
					throw new RuntimeException(
							String.format("Description Type concept %1$s not found", description.TypeId));
				}
				else 
					typeConcept = this.ConceptGroups.get(description.TypeId);
				System.out.println(description.TypeId);
				if (this.DescriptionTypes.containsKey(description.TypeId)) {
					throw new RuntimeException(String.format("Error adding %1$s to disctionary", description.TypeId));
				}
				else 
					this.DescriptionTypes.put(description.Id, typeConcept);
			} else {
				typeConcept = this.DescriptionTypes.get(description.TypeId);
			}
			this.GetConceptGroup(description.ConceptId).AddDescriptionGroup(descriptionGroup);
		}
	}

	/**
	 * Load raw snomed concept data into memory.
	 * 
	 * @param path
	 */
	private void LoadConcepts(String path) {
		FileReader reader = new FileReader();
		reader.openFile(path);
		String[] parts = reader.readLine().split("[\\t]");
		if ((parts.length != 5) || (parts[0].compareTo("id") != 0) || (parts[1].compareTo("effectiveTime") != 0)
				|| (parts[2].compareTo("active") != 0) || (parts[3].compareTo("moduleId") != 0)
				|| (parts[4].compareTo("definitionStatusId") != 0)) {
			throw new RuntimeException("Invalid header line to concept file");
		}

		RF2ConceptGroup conceptGroup = null;
		while (true) {
			String line = reader.readLine();
			if (line == null) {
				break;
			}
			RF2Concept concept = RF2Concept.Parse(this, line);
			if (concept != null) {
				/*
				 * Relationships in a common group are usually grouped together,
				 * so check last relationship to see if it is same group first.
				 */
				if ((conceptGroup != null) && (conceptGroup.Id != concept.Id)) {
					conceptGroup = null;
				}
				if ((conceptGroup == null) && (!this.ConceptGroups.contains(concept.Id))) {
					conceptGroup = this.ConceptGroups.get(concept.Id);
					conceptGroup = null;
				} else {
					conceptGroup = this.ConceptGroups.get(concept.Id);
				}

				if (conceptGroup == null) {
					conceptGroup = new RF2ConceptGroup();
					conceptGroup.Parser = this;
					conceptGroup.Id = concept.Id;
					
					if (this.ConceptGroups.containsKey(concept.Id)) {
						throw new RuntimeException("Error adding concept to dictionary");
					}
					else 
						this.ConceptGroups.put(conceptGroup.Id, conceptGroup);
					
				}
				conceptGroup.AddConcept(concept);
			}
		}
		FileReader.closeFile();
	}

	/**
	 * Load raw snomed concept data into memory.
	 * 
	 * @param path
	 * @return
	 */
	private Runnable FixConcepts() {
		return null;
	}

	/**
	 * Find snomed concept with indicated name.
	 * 
	 * @param name
	 * @return
	 */
	public final RF2ConceptGroup FindSnomedConcept(String name) {
		name = name.toUpperCase();

		ArrayList<RF2ConceptGroup> retVal = new ArrayList<RF2ConceptGroup>();
		for (RF2DescriptionGroup descriptionGroup : this.DescriptionGroups.values()) {
			for (RF2Description description : descriptionGroup.getItems()) {
				if (description == null ? null : description.Term.trim().toUpperCase().compareTo(name) == 0) {
					RF2ConceptGroup cg = this.GetConceptGroup(description.ConceptId);
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
			throw new RuntimeException("Multiple Snomed CT Concept records found");
		}

		return retVal.get(0);
	}
}