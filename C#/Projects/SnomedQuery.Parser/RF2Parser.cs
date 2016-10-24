using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Runtime;
using System.Runtime.Serialization;
using System.Runtime.Serialization.Formatters.Binary;
using System.Text;
using System.Threading.Tasks;

namespace SnomedQuery.Parser
{
  /// <summary>
  /// Parser fro Snomed rf2 files.
  /// </summary>
  public class RF2Parser
  {
    /// <summary>
    /// Snomed concept id for IsA relationship concept.
    /// </summary>
    public const Int64 IsAConceptId = 116680003;

    /// <summary>
    /// Snomed concept id for Synonym type concept
    /// </summary>
    public const Int64 SynonymTypeId = 900000000000013009;


    /// <summary>
    /// Snomed concept id for Fully Specified Name Concept
    /// </summary>
    public const Int64 FullySpecifiedNameConceptId = 900000000000003001;

    /// <summary>
    /// Top level Snomed concept.
    /// </summary>
    public RF2ConceptGroup RootConcept;

    /// <summary>
    /// Dictionary of relationships. There may be more than one relationship per id (its a list). If so, the
    /// relationships are ordered by their effective time (later times first) so [0] is always current.
    /// </summary>
    public ConcurrentDictionary<Int64, RF2RelationshipGroup> RelationshipGroups = new ConcurrentDictionary<Int64, RF2RelationshipGroup>();

    /// <summary>
    /// Dictionary of descriptions. There may be more than one description per id (its a list). If so, the
    /// descriptions are ordered by their effective time (later times first) so [0] is always current.
    /// </summary>
    public ConcurrentDictionary<Int64, RF2DescriptionGroup> DescriptionGroups = new ConcurrentDictionary<Int64, RF2DescriptionGroup>();

    /// <summary>
    /// Dictionary of concepts. There may be more than one concept per id (its a list). If so, the
    /// concepts are ordered by their effective time (later times first) so [0] is always current.
    /// </summary>
    public ConcurrentDictionary<Int64, RF2ConceptGroup> ConceptGroups = new ConcurrentDictionary<Int64, RF2ConceptGroup>();

    /// <summary>
    /// Dictionary of description concepts (i.e. Fully SpecifiedName, Synonymn, etc).
    /// </summary>
    public ConcurrentDictionary<Int64, RF2ConceptGroup> DescriptionTypes = new ConcurrentDictionary<Int64, RF2ConceptGroup>();

    /// <summary>
    /// Constructor.
    /// </summary>
    public RF2Parser()
    {
    }

    /// <summary>
    /// Load Snomed data from rf2 files.
    /// </summary>
    /// <param name="p"></param>
    /// <param name="path"></param>
    public void Load(String conceptPath,
      String relationshipPath,
      String descriptionPath)
    {
        this.LoadFromRF2Files(conceptPath, relationshipPath, descriptionPath);
    }

    /// <summary>
    /// Load raw snomed description data into memory.
    /// </summary>
    /// <param name="path"></param>
    public void LoadFromRF2Files(String conceptPath,
      String relationshipPath,
      String descriptionPath)
    {
      Task.WaitAll(
        this.LoadConcepts(conceptPath),
        this.LoadRelationships(relationshipPath),
        this.LoadDescriptions(descriptionPath)
        );

      Task fixConceptsTask = new Task(this.FixConcepts);
      fixConceptsTask.Start();

      Task fixRelationshipsTask = new Task(this.FixRelationships);
      fixRelationshipsTask.Start();

      Task fixDescriptionsTask = new Task(this.FixDescriptions);
      fixDescriptionsTask.Start();

      Task.WaitAll(fixConceptsTask, fixRelationshipsTask, fixDescriptionsTask);

      this.RootConcept = this.FindSnomedConcept("SNOMED CT CONCEPT");
    }


    /// <summary>
    /// Load raw snomed description data into memory.
    /// </summary>
    /// <param name="path"></param>
    async Task LoadRelationships(String path)
    {
      StreamReader sr = File.OpenText(path);
      String[] parts = sr.ReadLine().Split('\t');
      if (
        (parts.Length != 10) ||
        (String.Compare(parts[0], "id") != 0) ||
        (String.Compare(parts[1], "effectiveTime") != 0) ||
        (String.Compare(parts[2], "active") != 0) ||
        (String.Compare(parts[3], "moduleId") != 0) ||
        (String.Compare(parts[4], "sourceId") != 0) ||
        (String.Compare(parts[5], "destinationId") != 0) ||
        (String.Compare(parts[6], "relationshipGroup") != 0) ||
        (String.Compare(parts[7], "typeId") != 0) ||
        (String.Compare(parts[8], "characteristicTypeId") != 0) ||
        (String.Compare(parts[9], "modifierId") != 0)
        )
        throw new ApplicationException("Invalid header line to Relationship file");

      RF2RelationshipGroup relationshipGroup = null;
      while (true)
      {
        Task<String> lineTask = sr.ReadLineAsync();
        await lineTask;
        String line = lineTask.Result;
        if (line == null)
          break;

        RF2Relationship relationship = RF2Relationship.Parse(this, line);
        if (relationship != null)
        {
          /*
           * Relationships in a common group are usually grouped together, so check
           * last relationship to see if it is same group first.
           */
          if ((relationshipGroup != null) && (relationshipGroup.Id != relationship.Id))
            relationshipGroup = null;

          if (
            (relationshipGroup == null) &&
            (this.RelationshipGroups.TryGetValue(relationship.Id, out relationshipGroup) == false)
            )
            relationshipGroup = null;

          if (relationshipGroup == null)
          {
            relationshipGroup = new RF2RelationshipGroup()
            {
              Parser = this,
              Id = relationship.Id
            };
            if (this.RelationshipGroups.TryAdd(relationship.Id, relationshipGroup) == false)
              throw new ApplicationException("Error adding relationship to dictionary");
          }
          relationshipGroup.AddRelationship(relationship);
        }
      }
    }

    /// <summary>
    /// Pathc relationships.
    /// This must be run after concepts are read in.
    /// </summary>
    /// <param name="path"></param>
    void FixRelationships()
    {
      foreach (KeyValuePair<Int64, RF2RelationshipGroup> kvp in this.RelationshipGroups)
      {
        RF2RelationshipGroup relationshipGroup = kvp.Value;

        RF2Relationship relationship = relationshipGroup.Active;
        if (relationship != null)
        {
          RF2ConceptGroup sourceConcept = this.GetConceptGroup(relationship.SourceId);
          RF2ConceptGroup destinationConcept = this.GetConceptGroup(relationship.DestinationId);

          sourceConcept.AddSourceRelationship(relationshipGroup);
          destinationConcept.AddDestinationRelationship(relationshipGroup);
        }
      }
    }

    /// <summary>
    /// Return RelationshipGroup with indicated id.
    /// </summary>
    /// <param name="relationshipId"></param>
    /// <returns></returns>
    public RF2RelationshipGroup GetRelationshipGroup(Int64 relationshipId)
    {
      RF2RelationshipGroup relationshipGroup;
      if (this.RelationshipGroups.TryGetValue(relationshipId, out relationshipGroup) == false)
        throw new ApplicationException($"Relationship {relationshipId} not found in dictionary");
      return relationshipGroup;
    }

    /// <summary>
    /// Return ConceptGroup with indicated id.
    /// </summary>
    /// <param name="conceptId"></param>
    /// <returns></returns>
    public RF2ConceptGroup GetConceptGroup(Int64 conceptId)
    {
      RF2ConceptGroup conceptGroup;
      if (this.ConceptGroups.TryGetValue(conceptId, out conceptGroup) == false)
        throw new ApplicationException($"Concept {conceptId} not found in dictionary");
      return conceptGroup;
    }

    /// <summary>
    /// Return DescriptionGroup with indicated id.
    /// </summary>
    /// <param name="descriptionId"></param>
    /// <returns></returns>
    public RF2DescriptionGroup GetDescriptionGroup(Int64 descriptionId)
    {
      RF2DescriptionGroup descriptionGroup;
      if (this.DescriptionGroups.TryGetValue(descriptionId, out descriptionGroup) == false)
        throw new ApplicationException($"Description {descriptionId} not found in dictionary");
      return descriptionGroup;
    }

    /// <summary>
    /// Load raw snomed description data into memory.
    /// </summary>
    /// <param name="path"></param>
    async Task LoadDescriptions(String path)
    {
      StreamReader sr = File.OpenText(path);
      String[] parts = sr.ReadLine().Split('\t');
      if (
        (parts.Length != 9) ||
        (String.Compare(parts[0], "id") != 0) ||
        (String.Compare(parts[1], "effectiveTime") != 0) ||
        (String.Compare(parts[2], "active") != 0) ||
        (String.Compare(parts[3], "moduleId") != 0) ||
        (String.Compare(parts[4], "conceptId") != 0) ||
        (String.Compare(parts[5], "languageCode") != 0) ||
        (String.Compare(parts[6], "typeId") != 0) ||
        (String.Compare(parts[7], "term") != 0) ||
        (String.Compare(parts[8], "caseSignificanceId") != 0)
        )
        throw new ApplicationException("Invalid header line to description file");


      RF2DescriptionGroup descriptionGroup = null;
      while (true)
      {
        Task<String> lineTask = sr.ReadLineAsync();
        await lineTask;
        String line = lineTask.Result;
        if (line == null)
          break;
        RF2Description description = RF2Description.Parse(this, line);
        if (description != null)
        {
          /*
           * Relationships in a common group are usually grouped together, so check
           * last relationship to see if it is same group first.
           */
          if ((descriptionGroup != null) && (descriptionGroup.Id != description.Id))
            descriptionGroup = null;

          if (
            (descriptionGroup == null) &&
            (this.DescriptionGroups.TryGetValue(description.Id, out descriptionGroup) == false)
            )
            descriptionGroup = null;

          if (descriptionGroup == null)
          {
            descriptionGroup = new RF2DescriptionGroup()
            {
              Parser = this,
              Id = description.Id
            };
            if (this.DescriptionGroups.TryAdd(description.Id, descriptionGroup) == false)
              throw new ApplicationException("Error adding description to dictionary");
          }
          descriptionGroup.AddDescription(description);
        }
      }
    }

    /// <summary>
    /// Load raw snomed description data into memory.
    /// </summary>
    /// <param name="path"></param>
    void FixDescriptions()
    {
      foreach (KeyValuePair<Int64, RF2DescriptionGroup> kvp in this.DescriptionGroups)
      {
        RF2DescriptionGroup descriptionGroup = kvp.Value;
        this.FixDescription(descriptionGroup);
      }
    }

    /// <summary>
    /// Load raw snomed description data into memory.
    /// </summary>
    /// <param name="path"></param>
    void FixDescription(RF2DescriptionGroup descriptionGroup)
    {
      RF2Description description = descriptionGroup.Active;
      if (description == null)
        return;

      RF2ConceptGroup conceptGroup;
      if (this.ConceptGroups.TryGetValue(description.ConceptId, out conceptGroup) == false)
      {
        throw new ApplicationException($"Concept {description.ConceptId} in Description {description.Id} not found");
      }
      else
      {
        RF2ConceptGroup typeConcept;
        if (this.DescriptionTypes.TryGetValue(description.TypeId, out typeConcept) == false)
        {
          if (this.ConceptGroups.TryGetValue(description.TypeId, out typeConcept) == false)
            throw new ApplicationException($"Description Type concept {description.TypeId} not found");
          if (this.DescriptionTypes.TryAdd(description.TypeId, typeConcept) == false)
            throw new ApplicationException($"Error adding {description.TypeId} to disctionary");
        }
        this.GetConceptGroup(description.ConceptId).AddDescriptionGroup(descriptionGroup);
      }
    }

    /// <summary>
    /// Load raw snomed concept data into memory.
    /// </summary>
    /// <param name="path"></param>
    async Task LoadConcepts(String path)
    {
      StreamReader sr = File.OpenText(path);
      String[] parts = sr.ReadLine().Split('\t');
      if (
        (parts.Length != 5) ||
        (String.Compare(parts[0], "id") != 0) ||
        (String.Compare(parts[1], "effectiveTime") != 0) ||
        (String.Compare(parts[2], "active") != 0) ||
        (String.Compare(parts[3], "moduleId") != 0) ||
        (String.Compare(parts[4], "definitionStatusId") != 0)
        )
        throw new ApplicationException("Invalid header line to concept file");

      RF2ConceptGroup conceptGroup = null;
      while (true)
      {
        Task<String> lineTask = sr.ReadLineAsync();
        await lineTask;
        String line = lineTask.Result;
        if (line == null)
          break;
        RF2Concept concept = RF2Concept.Parse(this, line);
        if (concept != null)
        {
          /*
           * Relationships in a common group are usually grouped together, so check
           * last relationship to see if it is same group first.
           */
          if ((conceptGroup != null) && (conceptGroup.Id != concept.Id))
            conceptGroup = null;

          if (
            (conceptGroup == null) &&
            (this.ConceptGroups.TryGetValue(concept.Id, out conceptGroup) == false)
            )
            conceptGroup = null;

          if (conceptGroup == null)
          {
            conceptGroup = new RF2ConceptGroup()
            {
              Parser = this,
              Id = concept.Id
            };
            if (this.ConceptGroups.TryAdd(concept.Id, conceptGroup) == false)
              throw new ApplicationException("Error adding concept to dictionary");
          }
          conceptGroup.AddConcept(concept);
        }
      }
    }

    /// <summary>
    /// Load raw snomed concept data into memory.
    /// </summary>
    /// <param name="path"></param>
    void FixConcepts()
    {
    }

    /// <summary>
    /// Find snomed concept with indicated name.
    /// </summary>
    /// <param name="name"></param>
    /// <returns></returns>
    public RF2ConceptGroup FindSnomedConcept(String name)
    {
      name = name.ToUpper();

      List<RF2ConceptGroup> retVal = new List<RF2ConceptGroup>();
      foreach (RF2DescriptionGroup descriptionGroup in this.DescriptionGroups.Values)
      {
        foreach (RF2Description description in descriptionGroup.Items)
        {
          if (String.Compare(description?.Term.Trim().ToUpper(), name) == 0)
          {
            RF2ConceptGroup cg = this.GetConceptGroup(description.ConceptId);
            if (retVal.Contains(cg) == false)
              retVal.Add(cg);
          }
        }
      }

      if (retVal.Count == 0)
        throw new ApplicationException("No Snomed CT Concept records found");
      if (retVal.Count > 1)
        throw new ApplicationException("Multiple Snomed CT Concept records found");

      return retVal[0];
    }
  }
}
