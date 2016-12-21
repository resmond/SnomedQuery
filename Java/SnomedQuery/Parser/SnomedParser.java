package Parser;

import SnomedQuery.Model.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class parses snomed (in release RF2 format) into proprietary binary
 * records.
 */
public class SnomedParser {
	/**
	 * Parser of snomed rf2 files.
	 */
	private RF2Parser rf2Parser;

	/**
	 * Snomed Query Model Manager.
	 */
	private SnomedModelManager modelManager;

	/**
	 * Constructor
	 */
	public SnomedParser() {
	}
	
	/** 
	 Base directory of snomed raw data files.
	*/
	public final String getSnomedDataBaseDir()
	{
	  return FileReader.comboPaths(new String[]{this.modelManager.getRawDataDir()});
	}

	/** 
	 Return path to snomed concept rf2 file.
	*/
	public final String getSnomedConceptFile()
	{
		return FileReader.comboPaths(new String[]{this.getSnomedDataBaseDir(), "sct2_Concept_Full_INT_20160731.txt"});
		//return FileReader.comboPaths(new String[]{this.getSnomedDataBaseDir(), "testconcepts.txt"});
	}

	/** 
	 Return path to snomed relationship rf2 file.
	*/
	public final String getSnomedRelationshipFile()
	{
		return FileReader.comboPaths(new String[]{this.getSnomedDataBaseDir(), "sct2_Relationship_Full_INT_20160731.txt"});
	}

	/** 
	 Return path to snomed desription rf2 file.
	*/
	public final String getSnomedDescriptionFile()
	{
		return FileReader.comboPaths(new String[]{this.getSnomedDataBaseDir(), "sct2_Description_Full-en_INT_20160731.txt"});
	}

	/**
	 * Find snomed concept query instance by its snomed concept id.
	 * 
	 * @param conceptId
	 * @return
	 */
	private SnomedQueryConcept FindConcept(long conceptId) {
		return this.modelManager.getConceptById(conceptId);
	}

	/**
	 * Parse and serialize Snomed.
	 */
	public final void ParseAndSerialize() {
		String baseDir = FileReader.comboPaths(new String[]{"..","..","Data"});
		this.modelManager = new SnomedModelManager(baseDir);

		this.rf2Parser = new RF2Parser();
		this.rf2Parser.Load(this.getSnomedConceptFile(), this.getSnomedRelationshipFile(),
				this.getSnomedDescriptionFile());

		System.out.println("Creating concept records");
		try {
			this.CreateConceptRecords();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Creating relationship records");
		this.CreateRelationships();

		this.modelManager.serialize();
		this.rf2Parser = null;
	}

	/**
	 * Parse all SNOMED realtionships and create applicadia attributes for each
	 * one.
	 */
	private void CreateRelationships() {
		// We cant resize SnomedQueryConcept[]'s efficiently, so map all the
		// parent and
		// children to lists and set the objects IsAXXX to the list.ToArray()
		// when
		// we have collated all concepts.F
		java.util.concurrent.ConcurrentHashMap<Long, ArrayList<SnomedQueryConcept>> parentMap = new java.util.concurrent.ConcurrentHashMap<Long, ArrayList<SnomedQueryConcept>>();
		java.util.concurrent.ConcurrentHashMap<Long, ArrayList<SnomedQueryConcept>> childMap = new java.util.concurrent.ConcurrentHashMap<Long, ArrayList<SnomedQueryConcept>>();
		for (Map.Entry<Long, RF2RelationshipGroup> kvp : this.rf2Parser.RelationshipGroups.entrySet()) {
			RF2RelationshipGroup rf2RelationshipGroup = kvp.getValue();
			this.CreateRelationship(rf2RelationshipGroup, parentMap, childMap);
		}

		// Copy all parent lists to parent array in each concept.
		for (Entry<Long, ArrayList<SnomedQueryConcept>> kvp : parentMap.entrySet()) {
			SnomedQueryConcept item = this.FindConcept(kvp.getKey());
			item.setIsAParents(kvp.getValue().toArray(new SnomedQueryConcept[kvp.getValue().size()]));
		}

		// Copy all child lists to child array in each concept.
		for (Entry<Long, ArrayList<SnomedQueryConcept>> kvp : childMap.entrySet()) {
			SnomedQueryConcept item = this.FindConcept(kvp.getKey());
			item.setIsAChildren(kvp.getValue().toArray(new SnomedQueryConcept[kvp.getValue().size()]));
		}
	}

	/**
	 * Parse all SNOMED realtionships and create applicadia attributes for each
	 * one.
	 */
	private void CreateRelationship(RF2RelationshipGroup rf2RelationshipGroup,
			java.util.concurrent.ConcurrentHashMap<Long, ArrayList<SnomedQueryConcept>> parentMap,
			java.util.concurrent.ConcurrentHashMap<Long, ArrayList<SnomedQueryConcept>> childMap) {
		RF2Relationship rf2Relationship = rf2RelationshipGroup.getActive();
		if (rf2Relationship == null) {
			return;
		}

		switch ((int)rf2Relationship.TypeId) {
		case (int) RF2Parser.IsAConceptId:
			SnomedQueryConcept source = this.FindConcept(rf2Relationship.getSource().Id);
			SnomedQueryConcept dest = this.FindConcept(rf2Relationship.getDestination().Id);
			this.Add(parentMap, source, dest);
			this.Add(childMap, dest, source);
			break;
		}
	}

	/**
	 * Add related comcept to map.
	 * 
	 * @param map
	 * @param concept
	 * @param relatedConcept
	 */
	private void Add(ConcurrentHashMap<Long, ArrayList<SnomedQueryConcept>> map,
			SnomedQueryConcept concept, SnomedQueryConcept relatedConcept) {
		ArrayList<SnomedQueryConcept> items;
		if (!map.containsKey(concept.getConceptId())){
			items = new ArrayList<SnomedQueryConcept>();
			if (map.containsKey(concept.getConceptId())){
				throw new RuntimeException("Error adding item to Hash Map");
			}
			else
				map.put(concept.getConceptId(), items);
		}
		else {
			items = map.get(concept.getConceptId());
			items.add(relatedConcept);
			map.remove(concept.getConceptId());
			map.put(concept.getConceptId(), items);
		}
	}

	/**
	 * Iterate over all rf2Parser concepts and create correct GcSnomedConcept
	 * derived class for each element.
	 * @throws Exception 
	 */
	private void CreateConceptRecords() throws Exception {
		for (Entry<Long, RF2ConceptGroup> kvp : this.rf2Parser.ConceptGroups.entrySet()) {
			RF2ConceptGroup rf2ConceptGroup = kvp.getValue();
			SnomedQueryConcept concept = this.CreateConceptRecord(rf2ConceptGroup);
			if (concept != null) {
				this.modelManager.add(concept);
			}
		}
	}

	/**
	 * Iterate over all rf2Parser concepts and create correct GcSnomedConcept
	 * derived class for each element.
	 */
	private SnomedQueryConcept CreateConceptRecord(RF2ConceptGroup rf2ConceptGroup) {
		RF2Concept rf2Concept = rf2ConceptGroup.getActive();
		if (rf2Concept == null) {
			return null;
		}

		RF2ConceptGroup module = this.rf2Parser.GetConceptGroup(rf2Concept.ModuleId);
		RF2ConceptGroup definitionStatus = this.rf2Parser.GetConceptGroup(rf2Concept.DefinitionStatusId);

		ArrayList<String> synonyms = new ArrayList<String>();
		for (RF2DescriptionGroup descriptionGroup : rf2ConceptGroup.getDescriptionGroups()) {
			RF2Description description = descriptionGroup.GetActiveEnglishDescription();
			switch (String.valueOf(description.TypeId)) {
			case RF2Parser.SynonymTypeIds:
				synonyms.add(description.Term);
				break;

			case RF2Parser.FullySpecifiedNameConceptIds:
				break;

			default:
				System.out.println(String.format("Unimplemented description type %1$s",
						this.rf2Parser.GetConceptGroup(description.TypeId).GetFullySpecifiedName()));
				break;
			}
		}

		SnomedQueryConcept concept = new SnomedQueryConcept(rf2Concept.Id, rf2ConceptGroup.GetFullySpecifiedName(),
				synonyms.toArray(new String[0]), module.GetFullySpecifiedName(),
				definitionStatus.GetFullySpecifiedName(), rf2Concept.EffectiveTime);

		return concept;
	}
}