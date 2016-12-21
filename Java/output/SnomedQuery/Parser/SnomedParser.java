package SnomedQuery.Parser;

import SnomedQuery.Model.*;
import java.util.*;

  /** 
   This class parses snomed (in release RF2 format) into proprietary
   binary records.
  */
  public class SnomedParser
  {
	/** 
	 Parser of snomed rf2 files.
	*/
	private RF2Parser rf2Parser;

	/** 
	 Snomed Query Model Manager.
	*/
	private SnomedModelManager modelManager;

	/** 
	 Base directory of snomed raw data files.
	*/
	public final String getSnomedDataBaseDir()
	{
	  return Path.Combine(this.modelManager.getRawDataDir(), "SnomedCT_RF2Release_INT1000124_20160601", "Full", "Terminology");
	}

	/** 
	 Return path to snomed concept rf2 file.
	*/
	public final String getSnomedConceptFile()
	{
		return (new java.io.File(Path.Combine(this.getSnomedDataBaseDir(), "sct2_Concept_Full_INT_20160731.txt"))).getAbsolutePath();
	}

	/** 
	 Return path to snomed relationship rf2 file.
	*/
	public final String getSnomedRelationshipFile()
	{
		return (new java.io.File(Path.Combine(this.getSnomedDataBaseDir(), "sct2_Relationship_Full_INT_20160731.txt"))).getAbsolutePath();
	}

	/** 
	 Return path to snomed desription rf2 file.
	*/
	public final String getSnomedDescriptionFile()
	{
		return (new java.io.File(Path.Combine(this.getSnomedDataBaseDir(), "sct2_Description_Full-en_INT_20160731.txt"))).getAbsolutePath();
	}

	/** 
	 Constructor
	*/
	public SnomedParser()
	{
	}

	/** 
	 Find snomed concept query instance by its snomed concept id.
	 
	 @param conceptId
	 @return 
	*/
	private SnomedQueryConcept FindConcept(long conceptId)
	{
	  return this.modelManager.GetConceptById(conceptId);
	}

	/** 
	 Parse and serialize Snomed.
	*/
	public final void ParseAndSerialize()
	{
	  String baseDir = Path.Combine(Assembly.GetExecutingAssembly().Location, "..", "..", "..", "..", "Data");

	  this.modelManager = new SnomedModelManager(baseDir);

	  this.rf2Parser = new RF2Parser();
	  this.rf2Parser.Load(this.getSnomedConceptFile(), this.getSnomedRelationshipFile(), this.getSnomedDescriptionFile());

	  System.out.println("Creating concept records");
	  this.CreateConceptRecords();

	  System.out.println("Creating relationship records");
	  this.CreateRelationships();

	  this.modelManager.Serialize();
	  this.rf2Parser = null;
	}

	/** 
	 Parse all SNOMED realtionships and create applicadia attributes for each one.
	*/
	private void CreateRelationships()
	{
	  // We cant resize SnomedQueryConcept[]'s efficiently, so map all the parent and 
	  // children to lists and set the objects IsAXXX to the list.ToArray() when
	  // we have collated all concepts.
	  java.util.concurrent.ConcurrentHashMap<Long, ArrayList<SnomedQueryConcept>> parentMap = new java.util.concurrent.ConcurrentHashMap<Long, ArrayList<SnomedQueryConcept>>();
	  java.util.concurrent.ConcurrentHashMap<Long, ArrayList<SnomedQueryConcept>> childMap = new java.util.concurrent.ConcurrentHashMap<Long, ArrayList<SnomedQueryConcept>>();
	  for (Map.Entry<Long, RF2RelationshipGroup> kvp : this.rf2Parser.RelationshipGroups)
	  {
		RF2RelationshipGroup rf2RelationshipGroup = kvp.getValue();
		this.CreateRelationship(rf2RelationshipGroup, parentMap, childMap);
	  }

	  // Copy all parent lists to parent array in each concept.
	  for (Map.Entry<Long, ArrayList<SnomedQueryConcept>> kvp : parentMap)
	  {
		SnomedQueryConcept item = this.FindConcept(kvp.getKey());
		item.IsAParents = kvp.getValue().ToArray();
	  }


	  // Copy all child lists to child array in each concept.
	  for (Map.Entry<Long, ArrayList<SnomedQueryConcept>> kvp : childMap)
	  {
		SnomedQueryConcept item = this.FindConcept(kvp.getKey());
		item.IsAChildren = kvp.getValue().ToArray();
	  }
	}

	/** 
	 Parse all SNOMED realtionships and create applicadia attributes for each one.
	*/
	private void CreateRelationship(RF2RelationshipGroup rf2RelationshipGroup, java.util.concurrent.ConcurrentHashMap<Long, ArrayList<SnomedQueryConcept>> parentMap, java.util.concurrent.ConcurrentHashMap<Long, ArrayList<SnomedQueryConcept>> childMap)
	{
	  RF2Relationship rf2Relationship = rf2RelationshipGroup.getActive();
	  if (rf2Relationship == null)
	  {
		return;
	  }

	  switch (rf2Relationship.TypeId)
	  {
		case RF2Parser.IsAConceptId:
		  SnomedQueryConcept source = this.FindConcept(rf2Relationship.getSource().Id);
		  SnomedQueryConcept dest = this.FindConcept(rf2Relationship.getDestination().Id);
		  this.Add(parentMap, source, dest);
		  this.Add(childMap, dest, source);
		  break;
	  }
	}

	/** 
	 Add related comcept to map.
	 
	 @param map
	 @param concept
	 @param relatedConcept
	*/
	private void Add(java.util.concurrent.ConcurrentHashMap<Long, ArrayList<SnomedQueryConcept>> map, SnomedQueryConcept concept, SnomedQueryConcept relatedConcept)
	{
	  ArrayList<SnomedQueryConcept> items = null;
	  tangible.RefObject<ArrayList<SnomedQueryConcept>> tempRef_items = new tangible.RefObject<ArrayList<SnomedQueryConcept>>(items);
//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
	  if (map.TryGetValue(concept.ConceptId, tempRef_items) == false)
	  {
		  items = tempRef_items.argValue;
		items = new ArrayList<SnomedQueryConcept>();
//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
		if (map.TryAdd(concept.ConceptId, items) == false)
		{
		  throw new RuntimeException("Error adding item to dictionary");
		}
	  }
	  else
	  {
		  items = tempRef_items.argValue;
	  }
	  items.add(relatedConcept);
	}

	/** 
	 Iterate over all rf2Parser concepts and create correct GcSnomedConcept derived class for each element.
	*/
	private void CreateConceptRecords()
	{
	  for (Map.Entry<Long, RF2ConceptGroup> kvp : this.rf2Parser.ConceptGroups)
	  {
		RF2ConceptGroup rf2ConceptGroup = kvp.getValue();
		SnomedQueryConcept concept = this.CreateConceptRecord(rf2ConceptGroup);
		if (concept != null)
		{
		  this.modelManager.Add(concept);
		}
	  }
	}

	/** 
	 Iterate over all rf2Parser concepts and create correct GcSnomedConcept derived class for each element.
	*/
	private SnomedQueryConcept CreateConceptRecord(RF2ConceptGroup rf2ConceptGroup)
	{
	  RF2Concept rf2Concept = rf2ConceptGroup.getActive();
	  if (rf2Concept == null)
	  {
		return null;
	  }

	  RF2ConceptGroup module = this.rf2Parser.GetConceptGroup(rf2Concept.ModuleId);
	  RF2ConceptGroup definitionStatus = this.rf2Parser.GetConceptGroup(rf2Concept.DefinitionStatusId);

	  ArrayList<String> synonyms = new ArrayList<String>();
	  for (RF2DescriptionGroup descriptionGroup : rf2ConceptGroup.getDescriptionGroups())
	  {
		RF2Description description = descriptionGroup.GetActiveEnglishDescription();
		switch (description.TypeId)
		{
		  case RF2Parser.SynonymTypeId:
			synonyms.add(description.Term);
			break;

		  case RF2Parser.FullySpecifiedNameConceptId:
			break;

		  default:
			System.out.println(String.format("Unimplemented description type %1$s", this.rf2Parser.GetConceptGroup(description.TypeId).GetFullySpecifiedName()));
			break;
		}
	  }

	  SnomedQueryConcept concept = new SnomedQueryConcept(rf2Concept.Id, rf2ConceptGroup.GetFullySpecifiedName(), synonyms.toArray(new String[0]), module.GetFullySpecifiedName(), definitionStatus.GetFullySpecifiedName(), rf2Concept.EffectiveTime); // conceptEffectiveTime -  String conceptDefinitionStatus, -  String conceptModule, -  conceptSynonymns, -  conceptFullyQualifiedName, -  conceptId,

	  return concept;
	}
  }