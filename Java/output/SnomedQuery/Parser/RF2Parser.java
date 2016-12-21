package SnomedQuery.Parser;

import java.util.*;

  /** 
   Parser fro Snomed rf2 files.
  */
  public class RF2Parser
  {
	/** 
	 Snomed concept id for IsA relationship concept.
	*/
	public static final long IsAConceptId = 116680003;

	/** 
	 Snomed concept id for Synonym type concept
	*/
	public static final long SynonymTypeId = 900000000000013009L;


	/** 
	 Snomed concept id for Fully Specified Name Concept
	*/
	public static final long FullySpecifiedNameConceptId = 900000000000003001L;

	/** 
	 Top level Snomed concept.
	*/
	public RF2ConceptGroup RootConcept;

	/** 
	 Dictionary of relationships. There may be more than one relationship per id (its a list). If so, the
	 relationships are ordered by their effective time (later times first) so [0] is always current.
	*/
	public java.util.concurrent.ConcurrentHashMap<Long, RF2RelationshipGroup> RelationshipGroups = new java.util.concurrent.ConcurrentHashMap<Long, RF2RelationshipGroup>();

	/** 
	 Dictionary of descriptions. There may be more than one description per id (its a list). If so, the
	 descriptions are ordered by their effective time (later times first) so [0] is always current.
	*/
	public java.util.concurrent.ConcurrentHashMap<Long, RF2DescriptionGroup> DescriptionGroups = new java.util.concurrent.ConcurrentHashMap<Long, RF2DescriptionGroup>();

	/** 
	 Dictionary of concepts. There may be more than one concept per id (its a list). If so, the
	 concepts are ordered by their effective time (later times first) so [0] is always current.
	*/
	public java.util.concurrent.ConcurrentHashMap<Long, RF2ConceptGroup> ConceptGroups = new java.util.concurrent.ConcurrentHashMap<Long, RF2ConceptGroup>();

	/** 
	 Dictionary of description concepts (i.e. Fully SpecifiedName, Synonymn, etc).
	*/
	public java.util.concurrent.ConcurrentHashMap<Long, RF2ConceptGroup> DescriptionTypes = new java.util.concurrent.ConcurrentHashMap<Long, RF2ConceptGroup>();

	/** 
	 Constructor.
	*/
	public RF2Parser()
	{
	}

	/** 
	 Load Snomed data from rf2 files.
	 
	 @param p
	 @param path
	*/
	public final void Load(String conceptPath, String relationshipPath, String descriptionPath)
	{
		this.LoadFromRF2Files(conceptPath, relationshipPath, descriptionPath);
	}

	/** 
	 Load raw snomed description data into memory.
	 
	 @param path
	*/
	public final void LoadFromRF2Files(String conceptPath, String relationshipPath, String descriptionPath)
	{
	  // Load main three snomed files (concepts, relationships, and descriptions).
	  // Load files concurrently
	  {
		Thread loadConceptsTask = new Thread(() -> this.LoadConcepts(conceptPath));
		Thread loadRelationshipsTask = new Thread(() -> this.LoadRelationships(relationshipPath));
		Thread loadDescriptionsTask = new Thread(() -> this.LoadDescriptions(descriptionPath));

		loadConceptsTask.start();
		loadRelationshipsTask.start();
		loadDescriptionsTask.start();

		loadConceptsTask.join();
		loadRelationshipsTask.join();
		loadDescriptionsTask.join();
	  }

	  {
	  // Perform cleanup on above loaded files. 
		Thread fixConceptsTask = new Thread(this.FixConcepts);
		Thread fixRelationshipsTask = new Thread(this.FixRelationships);
		Thread fixDescriptionsTask = new Thread(this.FixDescriptions);

		fixConceptsTask.start();
		fixRelationshipsTask.start();
		fixDescriptionsTask.start();

		fixConceptsTask.join();
		fixRelationshipsTask.join();
		fixDescriptionsTask.join();
	  }

	  this.RootConcept = this.FindSnomedConcept("SNOMED CT CONCEPT");
	}


	/** 
	 Load raw snomed description data into memory.
	 
	 @param path
	*/
	private void LoadRelationships(String path)
	{
	  FileReader.OpenFile(path);
	  String[] parts = FileReader.ReadLine().split("[\\t]", -1);
	  if ((parts.length != 10) || (parts[0].compareTo("id") != 0) || (parts[1].compareTo("effectiveTime") != 0) || (parts[2].compareTo("active") != 0) || (parts[3].compareTo("moduleId") != 0) || (parts[4].compareTo("sourceId") != 0) || (parts[5].compareTo("destinationId") != 0) || (parts[6].compareTo("relationshipGroup") != 0) || (parts[7].compareTo("typeId") != 0) || (parts[8].compareTo("characteristicTypeId") != 0) || (parts[9].compareTo("modifierId") != 0))
	  {
		throw new RuntimeException("Invalid header line to Relationship file");
	  }

	  RF2RelationshipGroup relationshipGroup = null;
	  while (true)
	  {
		String line = FileReader.ReadLine();
		if (line == null)
		{
		  break;
		}

		RF2Relationship relationship = RF2Relationship.Parse(this, line);
		if (relationship != null)
		{
		  /*
		   * Relationships in a common group are usually grouped together, so check
		   * last relationship to see if it is same group first.
		   */
		  if ((relationshipGroup != null) && (relationshipGroup.Id != relationship.Id))
		  {
			relationshipGroup = null;
		  }

		  tangible.RefObject<SnomedQuery.Parser.RF2RelationshipGroup> tempRef_relationshipGroup = new tangible.RefObject<SnomedQuery.Parser.RF2RelationshipGroup>(relationshipGroup);
//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
		  if ((relationshipGroup == null) && (this.RelationshipGroups.TryGetValue(relationship.Id, tempRef_relationshipGroup) == false))
		  {
			  relationshipGroup = tempRef_relationshipGroup.argValue;
			relationshipGroup = null;
		  }
		  else
		  {
			  relationshipGroup = tempRef_relationshipGroup.argValue;
		  }

		  if (relationshipGroup == null)
		  {
			relationshipGroup = new RF2RelationshipGroup();
			relationshipGroup.Parser = this;
			relationshipGroup.Id = relationship.Id;
//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
			if (this.RelationshipGroups.TryAdd(relationship.Id, relationshipGroup) == false)
			{
			  throw new RuntimeException("Error adding relationship to dictionary");
			}
		  }
		  relationshipGroup.AddRelationship(relationship);
		}
	  }
	}

	/** 
	 Pathc relationships.
	 This must be run after concepts are read in.
	 
	 @param path
	*/
	private void FixRelationships()
	{
	  for (Map.Entry<Long, RF2RelationshipGroup> kvp : this.RelationshipGroups)
	  {
		RF2RelationshipGroup relationshipGroup = kvp.getValue();

		RF2Relationship relationship = relationshipGroup.getActive();
		if (relationship != null)
		{
		  RF2ConceptGroup sourceConcept = this.GetConceptGroup(relationship.SourceId);
		  RF2ConceptGroup destinationConcept = this.GetConceptGroup(relationship.DestinationId);

		  sourceConcept.AddSourceRelationship(relationshipGroup);
		  destinationConcept.AddDestinationRelationship(relationshipGroup);
		}
	  }
	}

	/** 
	 Return RelationshipGroup with indicated id.
	 
	 @param relationshipId
	 @return 
	*/
	public final RF2RelationshipGroup GetRelationshipGroup(long relationshipId)
	{
	  RF2RelationshipGroup relationshipGroup = null;
	  tangible.RefObject<SnomedQuery.Parser.RF2RelationshipGroup> tempRef_relationshipGroup = new tangible.RefObject<SnomedQuery.Parser.RF2RelationshipGroup>(relationshipGroup);
//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
	  if (this.RelationshipGroups.TryGetValue(relationshipId, tempRef_relationshipGroup) == false)
	  {
		  relationshipGroup = tempRef_relationshipGroup.argValue;
		throw new RuntimeException(String.format("Relationship %1$s not found in dictionary", relationshipId));
	  }
	  else
	  {
		  relationshipGroup = tempRef_relationshipGroup.argValue;
	  }
	  return relationshipGroup;
	}

	/** 
	 Return ConceptGroup with indicated id.
	 
	 @param conceptId
	 @return 
	*/
	public final RF2ConceptGroup GetConceptGroup(long conceptId)
	{
	  RF2ConceptGroup conceptGroup = null;
	  tangible.RefObject<SnomedQuery.Parser.RF2ConceptGroup> tempRef_conceptGroup = new tangible.RefObject<SnomedQuery.Parser.RF2ConceptGroup>(conceptGroup);
//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
	  if (this.ConceptGroups.TryGetValue(conceptId, tempRef_conceptGroup) == false)
	  {
		  conceptGroup = tempRef_conceptGroup.argValue;
		throw new RuntimeException(String.format("Concept %1$s not found in dictionary", conceptId));
	  }
	  else
	  {
		  conceptGroup = tempRef_conceptGroup.argValue;
	  }
	  return conceptGroup;
	}

	/** 
	 Return DescriptionGroup with indicated id.
	 
	 @param descriptionId
	 @return 
	*/
	public final RF2DescriptionGroup GetDescriptionGroup(long descriptionId)
	{
	  RF2DescriptionGroup descriptionGroup;
	  if (this.DescriptionGroups.containsKey(descriptionId))
	  {
		descriptionGroup = this.DescriptionGroups.get(descriptionId);
	  }
	  else
	  {
		throw new RuntimeException(String.format("Description %1$s not found in dictionary", descriptionId));
	  }
	  return descriptionGroup;
	}

	/** 
	 Load raw snomed description data into memory.
	 
	 @param path
	*/
	private void LoadDescriptions(String path)
	{
	  FileReader.OpenFile(path);
	  String[] parts = FileReader.ReadLine().split("[\\t]", -1);
	  if ((parts.length != 9) || (parts[0].compareTo("id") != 0) || (parts[1].compareTo("effectiveTime") != 0) || (parts[2].compareTo("active") != 0) || (parts[3].compareTo("moduleId") != 0) || (parts[4].compareTo("conceptId") != 0) || (parts[5].compareTo("languageCode") != 0) || (parts[6].compareTo("typeId") != 0) || (parts[7].compareTo("term") != 0) || (parts[8].compareTo("caseSignificanceId") != 0))
	  {
		throw new RuntimeException("Invalid header line to description file");
	  }


	  RF2DescriptionGroup descriptionGroup = null;
	  while (true)
	  {
		String line = FileReader.ReadLine();
		if (line == null)
		{
		  break;
		}
		RF2Description description = RF2Description.Parse(this, line);
		if (description != null)
		{
		  /*
		   * Relationships in a common group are usually grouped together, so check
		   * last relationship to see if it is same group first.
		   */
		  if ((descriptionGroup != null) && (descriptionGroup.Id != description.Id))
		  {
			descriptionGroup = null;
		  }

		  tangible.RefObject<SnomedQuery.Parser.RF2DescriptionGroup> tempRef_descriptionGroup = new tangible.RefObject<SnomedQuery.Parser.RF2DescriptionGroup>(descriptionGroup);
//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
		  if ((descriptionGroup == null) && (this.DescriptionGroups.TryGetValue(description.Id, tempRef_descriptionGroup) == false))
		  {
			  descriptionGroup = tempRef_descriptionGroup.argValue;
			descriptionGroup = null;
		  }
		  else
		  {
			  descriptionGroup = tempRef_descriptionGroup.argValue;
		  }

		  if (descriptionGroup == null)
		  {
			descriptionGroup = new RF2DescriptionGroup();
			descriptionGroup.Parser = this;
			descriptionGroup.Id = description.Id;
//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
			if (this.DescriptionGroups.TryAdd(description.Id, descriptionGroup) == false)
			{
			  throw new RuntimeException("Error adding description to dictionary");
			}
		  }
		  descriptionGroup.AddDescription(description);
		}
	  }
	  FileReader.CloseFile();
	}

	/** 
	 Load raw snomed description data into memory.
	 
	 @param path
	*/
	private void FixDescriptions()
	{
	  for (Map.Entry<Long, RF2DescriptionGroup> kvp : this.DescriptionGroups)
	  {
		RF2DescriptionGroup descriptionGroup = kvp.getValue();
		this.FixDescription(descriptionGroup);
	  }
	}

	/** 
	 Load raw snomed description data into memory.
	 
	 @param path
	*/
	private void FixDescription(RF2DescriptionGroup descriptionGroup)
	{
	  RF2Description description = descriptionGroup.getActive();
	  if (description == null)
	  {
		return;
	  }

	  RF2ConceptGroup conceptGroup = null;
	  tangible.RefObject<SnomedQuery.Parser.RF2ConceptGroup> tempRef_conceptGroup = new tangible.RefObject<SnomedQuery.Parser.RF2ConceptGroup>(conceptGroup);
//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
	  if (this.ConceptGroups.TryGetValue(description.ConceptId, tempRef_conceptGroup) == false)
	  {
		  conceptGroup = tempRef_conceptGroup.argValue;
		throw new RuntimeException(String.format("Concept %1$s in Description %2$s not found", description.ConceptId, description.Id));
	  }
	  else
	  {
		  conceptGroup = tempRef_conceptGroup.argValue;
		RF2ConceptGroup typeConcept = null;
		tangible.RefObject<SnomedQuery.Parser.RF2ConceptGroup> tempRef_typeConcept = new tangible.RefObject<SnomedQuery.Parser.RF2ConceptGroup>(typeConcept);
//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
		if (this.DescriptionTypes.TryGetValue(description.TypeId, tempRef_typeConcept) == false)
		{
			typeConcept = tempRef_typeConcept.argValue;
		  tangible.RefObject<SnomedQuery.Parser.RF2ConceptGroup> tempRef_typeConcept2 = new tangible.RefObject<SnomedQuery.Parser.RF2ConceptGroup>(typeConcept);
//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
		  if (this.ConceptGroups.TryGetValue(description.TypeId, tempRef_typeConcept2) == false)
		  {
			  typeConcept = tempRef_typeConcept2.argValue;
			throw new RuntimeException(String.format("Description Type concept %1$s not found", description.TypeId));
		  }
		  else
		  {
			  typeConcept = tempRef_typeConcept2.argValue;
		  }
//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
		  if (this.DescriptionTypes.TryAdd(description.TypeId, typeConcept) == false)
		  {
			throw new RuntimeException(String.format("Error adding %1$s to disctionary", description.TypeId));
		  }
		}
		else
		{
			typeConcept = tempRef_typeConcept.argValue;
		}
		this.GetConceptGroup(description.ConceptId).AddDescriptionGroup(descriptionGroup);
	  }
	}

	/** 
	 Load raw snomed concept data into memory.
	 
	 @param path
	*/
	private void LoadConcepts(String path)
	{
	  FileReader.OpenFile(path);
	  String[] parts = FileReader.ReadLine().split("[\\t]", -1);
	  if ((parts.length != 5) || (parts[0].compareTo("id") != 0) || (parts[1].compareTo("effectiveTime") != 0) || (parts[2].compareTo("active") != 0) || (parts[3].compareTo("moduleId") != 0) || (parts[4].compareTo("definitionStatusId") != 0))
	  {
		throw new RuntimeException("Invalid header line to concept file");
	  }

	  RF2ConceptGroup conceptGroup = null;
	  while (true)
	  {
		String line = FileReader.ReadLine();
		if (line == null)
		{
		  break;
		}
		RF2Concept concept = RF2Concept.Parse(this, line);
		if (concept != null)
		{
		  /*
		   * Relationships in a common group are usually grouped together, so check
		   * last relationship to see if it is same group first.
		   */
		  if ((conceptGroup != null) && (conceptGroup.Id != concept.Id))
		  {
			conceptGroup = null;
		  }

		  tangible.RefObject<SnomedQuery.Parser.RF2ConceptGroup> tempRef_conceptGroup = new tangible.RefObject<SnomedQuery.Parser.RF2ConceptGroup>(conceptGroup);
//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
		  if ((conceptGroup == null) && (this.ConceptGroups.TryGetValue(concept.Id, tempRef_conceptGroup) == false))
		  {
			  conceptGroup = tempRef_conceptGroup.argValue;
			conceptGroup = null;
		  }
		  else
		  {
			  conceptGroup = tempRef_conceptGroup.argValue;
		  }

		  if (conceptGroup == null)
		  {
			conceptGroup = new RF2ConceptGroup();
			conceptGroup.Parser = this;
			conceptGroup.Id = concept.Id;
//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
			if (this.ConceptGroups.TryAdd(concept.Id, conceptGroup) == false)
			{
			  throw new RuntimeException("Error adding concept to dictionary");
			}
		  }
		  conceptGroup.AddConcept(concept);
		}
	  }
	  FileReader.CloseFile();
	}

	/** 
	 Load raw snomed concept data into memory.
	 
	 @param path
	*/
	private void FixConcepts()
	{
	}

	/** 
	 Find snomed concept with indicated name.
	 
	 @param name
	 @return 
	*/
	public final RF2ConceptGroup FindSnomedConcept(String name)
	{
	  name = name.toUpperCase();

	  ArrayList<RF2ConceptGroup> retVal = new ArrayList<RF2ConceptGroup>();
	  for (RF2DescriptionGroup descriptionGroup : this.DescriptionGroups.values())
	  {
		for (RF2Description description : descriptionGroup.getItems())
		{
		  if (description == null ? null : description.Term.trim().toUpperCase().compareTo(name) == 0)
		  {
			RF2ConceptGroup cg = this.GetConceptGroup(description.ConceptId);
			if (retVal.contains(cg) == false)
			{
			  retVal.add(cg);
			}
		  }
		}
	  }

	  if (retVal.isEmpty())
	  {
		throw new RuntimeException("No Snomed CT Concept records found");
	  }
	  if (retVal.size() > 1)
	  {
		throw new RuntimeException("Multiple Snomed CT Concept records found");
	  }

	  return retVal.get(0);
	}
  }