package Parser;

import SnomedQuery.Model.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * <p>
 * This class parses snomed (in release RF2 format) into proprietary binary
 * records.
 * </p>
 * 
 * @author Travis Lukach
 */
public class SnomedParser {
	/**
	 * <p>
	 * Parser of snomed rf2 files.
	 * </p>
	 */
	private RF2Parser rf2Parser;

	/**
	 * <p>
	 * Snomed Query Model Manager.
	 * </p>
	 */
	private SnomedModelManager modelManager;

	/**
	 * <p>
	 * Constructor
	 * </p>
	 */
	public SnomedParser() {
	}

	/**
	 * <p>
	 * Base directory of snomed raw data files.
	 * </p>
	 */
	public final String getSnomedDataBaseDir() {
		return FileReader
				.comboPaths(new String[]{this.modelManager.getRawDataDir(),
						"SnomedCT_RF2Release_INT1000124_20160601", "Full",
						"Terminology",});
	}

	/**
	 * <p>
	 * Return path to snomed concept rf2 file.
	 * </p>
	 */
	public final String getSnomedConceptFile() {
		return FileReader.comboPaths(new String[]{this.getSnomedDataBaseDir(),
				"sct2_Concept_Full_INT_20160731.txt"});
	}

	/**
	 * <p>
	 * Return path to snomed relationship rf2 file.
	 * </p>
	 */
	public final String getSnomedRelationshipFile() {
		return FileReader.comboPaths(new String[]{this.getSnomedDataBaseDir(),
				"sct2_Relationship_Full_INT_20160731.txt"});
	}

	/**
	 * <p>
	 * Return path to snomed desription rf2 file.
	 * </p>
	 */
	public final String getSnomedDescriptionFile() {
		return FileReader.comboPaths(new String[]{this.getSnomedDataBaseDir(),
				"sct2_Description_Full-en_INT_20160731.txt"});
	}

	/**
	 * <p>
	 * Find snomed concept query instance by its snomed concept id.
	 * </p>
	 * 
	 * @param conceptId
	 * @return
	 */
	private SnomedQueryConcept findConcept(long conceptId) {
		try {
			return this.modelManager.getConceptById(conceptId);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * <p>
	 * Parse and serialize Snomed.
	 * </p>
	 */
	public final void parseAndSerialize() {
		String baseDir = FileReader
				.comboPaths(new String[]{"..", "..", "Data"});
		this.modelManager = new SnomedModelManager(baseDir);

		this.rf2Parser = new RF2Parser();
		this.rf2Parser.load(this.getSnomedConceptFile(),
				this.getSnomedRelationshipFile(),
				this.getSnomedDescriptionFile());

		System.out.println("Creating concept records");
		try {
			this.createConceptRecords();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Creating relationship records");
		this.createRelationships();

		this.modelManager.serialize();
		this.rf2Parser = null;
	}

	/**
	 * <p>
	 * Parse all SNOMED realtionships and create applicadia attributes for each
	 * one.
	 * </p>
	 */
	private void createRelationships() {
		// We cant resize SnomedQueryConcept[]'s efficiently, so map all the
		// parent and
		// children to lists and set the objects IsAXXX to the list.ToArray()
		// when
		// we have collated all concepts.F
		HashMap<Long, ArrayList<SnomedQueryConcept>> parentMap = new HashMap<Long, ArrayList<SnomedQueryConcept>>();
		HashMap<Long, ArrayList<SnomedQueryConcept>> childMap = new HashMap<Long, ArrayList<SnomedQueryConcept>>();
		for (Map.Entry<Long, RF2RelationshipGroup> kvp : this.rf2Parser.relationshipGroups
				.entrySet()) {
			RF2RelationshipGroup rf2RelationshipGroup = kvp.getValue();
			this.createRelationship(rf2RelationshipGroup, parentMap, childMap);
		}

		// Copy all parent lists to parent array in each concept.
		for (Entry<Long, ArrayList<SnomedQueryConcept>> kvp : parentMap
				.entrySet()) {
			SnomedQueryConcept item = this.findConcept(kvp.getKey());
			item.setIsAParents(kvp.getValue()
					.toArray(new SnomedQueryConcept[kvp.getValue().size()]));
		}

		// Copy all child lists to child array in each concept.
		for (Entry<Long, ArrayList<SnomedQueryConcept>> kvp : childMap
				.entrySet()) {
			SnomedQueryConcept item = this.findConcept(kvp.getKey());
			item.setIsAChildren(kvp.getValue()
					.toArray(new SnomedQueryConcept[kvp.getValue().size()]));
		}
	}

	/**
	 * <p>
	 * Parse all SNOMED realtionships and create applicadia attributes for each
	 * one.
	 * </p>
	 */
	private void createRelationship(RF2RelationshipGroup rf2RelationshipGroup,
			HashMap<Long, ArrayList<SnomedQueryConcept>> parentMap,
			HashMap<Long, ArrayList<SnomedQueryConcept>> childMap) {
		RF2Relationship rf2Relationship = rf2RelationshipGroup.getActive();
		if (rf2Relationship == null) {
			return;
		}
		switch ((int) rf2Relationship.getTypeId()) {
			case (int) RF2Parser.isAConceptId :
				SnomedQueryConcept source = this
						.findConcept(rf2Relationship.getSource().getId());
				SnomedQueryConcept dest = this
						.findConcept(rf2Relationship.getDestination().getId());
				this.add(parentMap, source, dest);
				this.add(childMap, dest, source);
				break;
		}
	}

	/**
	 * <p>
	 * Add related comcept to map.
	 * </p>
	 * 
	 * @param map
	 * @param concept
	 * @param relatedConcept
	 */
	private void add(HashMap<Long, ArrayList<SnomedQueryConcept>> map,
			SnomedQueryConcept concept, SnomedQueryConcept relatedConcept) {
		ArrayList<SnomedQueryConcept> items;
		if (!map.containsKey(concept.getConceptId())) {
			items = new ArrayList<SnomedQueryConcept>();
			if (map.containsKey(concept.getConceptId())) {
				throw new RuntimeException("Error adding item to Hash Map");
			} else
				map.put(concept.getConceptId(), items);
		} else {
			items = map.get(concept.getConceptId());
		}
		items.add(relatedConcept);
		map.remove(concept.getConceptId());
		map.put(concept.getConceptId(), items);
	}

	/**
	 * <p>
	 * Iterate over all rf2Parser concepts and create correct GcSnomedConcept
	 * derived class for each element.
	 * </p>
	 * 
	 * @throws Exception
	 */
	private void createConceptRecords() throws Exception {
		for (Entry<Long, RF2ConceptGroup> kvp : this.rf2Parser.conceptGroups
				.entrySet()) {
			RF2ConceptGroup rf2ConceptGroup = kvp.getValue();
			SnomedQueryConcept concept = this
					.createConceptRecord(rf2ConceptGroup);
			if (concept != null) {
				this.modelManager.add(concept);
			}
		}
	}

	/**
	 * <p>
	 * Iterate over all rf2Parser concepts and create correct GcSnomedConcept
	 * derived class for each element.
	 * </p>
	 */
	private SnomedQueryConcept createConceptRecord(
			RF2ConceptGroup rf2ConceptGroup) {
		RF2Concept rf2Concept = rf2ConceptGroup.getActive();
		if (rf2Concept == null) {
			return null;
		}

		RF2ConceptGroup module = this.rf2Parser
				.getConceptGroup(rf2Concept.getModuleId());
		RF2ConceptGroup definitionStatus = this.rf2Parser
				.getConceptGroup(rf2Concept.getDefinitionStatusId());

		ArrayList<String> synonyms = new ArrayList<String>();
		for (RF2DescriptionGroup descriptionGroup : rf2ConceptGroup
				.getDescriptionGroups()) {
			RF2Description description = descriptionGroup
					.getActiveEnglishDescription();
			switch (String.valueOf(description.getTypeId())) {
				case RF2Parser.synonymTypeIds :
					synonyms.add(description.getTerm());
					break;

				case RF2Parser.fullySpecifiedNameConceptIds :
					break;

				default :
					System.out.println(String.format(
							"Unimplemented description type %1$s",
							this.rf2Parser
									.getConceptGroup(description.getTypeId())
									.getFullySpecifiedName()));
					break;
			}
		}

		SnomedQueryConcept concept = new SnomedQueryConcept(rf2Concept.getId(),
				rf2ConceptGroup.getFullySpecifiedName(),
				synonyms.toArray(new String[synonyms.size()]),
				module.getFullySpecifiedName(),
				definitionStatus.getFullySpecifiedName(),
				rf2Concept.getEffectiveTime());

		return concept;
	}
}