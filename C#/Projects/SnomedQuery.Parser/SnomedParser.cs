using SnomedQuery.Model;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime;

namespace SnomedQuery.Parser
{
  /// <summary>
  /// This class parses snomed (in release RF2 format) into proprietary
  /// binary records.
  /// </summary>
  public class SnomedParser
  {
    /// <summary>
    /// Parser of snomed rf2 files.
    /// </summary>
    RF2Parser rf2Parser;

    /// <summary>
    /// Snomed Query Model Manager.
    /// </summary>
    private SnomedModelManager modelManager;

    /// <summary>
    /// Base directory of snomed raw data files.
    /// </summary>
    public String SnomedDataBaseDir
    {
      get
      {
        return Path.Combine(this.modelManager.RawDataDir,
          "SnomedCT_RF2Release_INT1000124_20160601",
          "Full",
          "Terminology");
      }
    }

    /// <summary>
    /// Return path to snomed concept rf2 file.
    /// </summary>
    public String SnomedConceptFile
    {
      get { return Path.GetFullPath(Path.Combine(this.SnomedDataBaseDir, "sct2_Concept_Full_INT_20160731.txt")); }
    }

    /// <summary>
    /// Return path to snomed relationship rf2 file.
    /// </summary>
    public String SnomedRelationshipFile
    {
      get { return Path.GetFullPath(Path.Combine(this.SnomedDataBaseDir, "sct2_Relationship_Full_INT_20160731.txt")); }
    }

    /// <summary>
    /// Return path to snomed desription rf2 file.
    /// </summary>
    public String SnomedDescriptionFile
    {
      get { return Path.GetFullPath(Path.Combine(this.SnomedDataBaseDir, "sct2_Description_Full-en_INT_20160731.txt")); }
    }

    /// <summary>
    /// Constructor
    /// </summary>
    public SnomedParser()
    {
    }

    /// <summary>
    /// Find snomed concept query instance by its snomed concept id.
    /// </summary>
    /// <param name="conceptId"></param>
    /// <returns></returns>
    SnomedQueryConcept FindConcept(Int64 conceptId)
    {
      return this.modelManager.GetConceptById(conceptId);
    }

    /// <summary>
    /// Parse and serialize Snomed.
    /// </summary>
    public void ParseAndSerialize()
    {
      String baseDir = Path.Combine(Assembly.GetExecutingAssembly().Location,
          "..",
          "..",
          "..",
          "..",
          "Data");

      this.modelManager = new SnomedModelManager(baseDir);

      this.rf2Parser = new RF2Parser();
      this.rf2Parser.Load(this.SnomedConceptFile, this.SnomedRelationshipFile, this.SnomedDescriptionFile);

      Console.WriteLine("Creating concept records");
      this.CreateConceptRecords();

      Console.WriteLine("Creating relationship records");
      this.CreateRelationships();

      this.modelManager.Serialize();
      this.rf2Parser = null;
    }

    /// <summary>
    /// Parse all SNOMED realtionships and create applicadia attributes for each one.
    /// </summary>
    void CreateRelationships()
    {
      // We cant resize SnomedQueryConcept[]'s efficiently, so map all the parent and 
      // children to lists and set the objects IsAXXX to the list.ToArray() when
      // we have collated all concepts.
      ConcurrentDictionary<Int64, List<SnomedQueryConcept>> parentMap =
        new ConcurrentDictionary<long, List<SnomedQueryConcept>>();
      ConcurrentDictionary<Int64, List<SnomedQueryConcept>> childMap =
        new ConcurrentDictionary<long, List<SnomedQueryConcept>>();
      foreach (KeyValuePair<Int64, RF2RelationshipGroup> kvp in this.rf2Parser.RelationshipGroups)
      {
        RF2RelationshipGroup rf2RelationshipGroup = kvp.Value;
        this.CreateRelationship(rf2RelationshipGroup, parentMap, childMap);
      }

      // Copy all parent lists to parent array in each concept.
      foreach (KeyValuePair<Int64, List<SnomedQueryConcept>> kvp in parentMap)
      {
        SnomedQueryConcept item = this.FindConcept(kvp.Key);
        item.IsAParents = kvp.Value.ToArray();
      }


      // Copy all child lists to child array in each concept.
      foreach (KeyValuePair<Int64, List<SnomedQueryConcept>> kvp in childMap)
      {
        SnomedQueryConcept item = this.FindConcept(kvp.Key);
        item.IsAChildren = kvp.Value.ToArray();
      }
    }

    /// <summary>
    /// Parse all SNOMED realtionships and create applicadia attributes for each one.
    /// </summary>
    void CreateRelationship(RF2RelationshipGroup rf2RelationshipGroup,
      ConcurrentDictionary<Int64, List<SnomedQueryConcept>> parentMap,
      ConcurrentDictionary<Int64, List<SnomedQueryConcept>> childMap)
    {
      RF2Relationship rf2Relationship = rf2RelationshipGroup.Active;
      if (rf2Relationship == null)
        return;

      switch (rf2Relationship.TypeId)
      {
        case RF2Parser.IsAConceptId:
          SnomedQueryConcept source = this.FindConcept(rf2Relationship.Source.Id);
          SnomedQueryConcept dest = this.FindConcept(rf2Relationship.Destination.Id);
          this.Add(parentMap, source, dest);
          this.Add(childMap, dest, source);
          break;
      }
    }

    /// <summary>
    /// Add related comcept to map.
    /// </summary>
    /// <param name="map"></param>
    /// <param name="concept"></param>
    /// <param name="relatedConcept"></param>
    void Add(ConcurrentDictionary<Int64, List<SnomedQueryConcept>> map,
      SnomedQueryConcept concept,
      SnomedQueryConcept relatedConcept)
    {
      List<SnomedQueryConcept> items;
      if (map.TryGetValue(concept.ConceptId, out items) == false)
      {
        items = new List<SnomedQueryConcept>();
        if (map.TryAdd(concept.ConceptId, items) == false)
          throw new ApplicationException("Error adding item to dictionary");
      }
      items.Add(relatedConcept);
    }

    /// <summary>
    /// Iterate over all rf2Parser concepts and create correct GcSnomedConcept derived class for each element.
    /// </summary>
    void CreateConceptRecords()
    {
      foreach (KeyValuePair<Int64, RF2ConceptGroup> kvp in this.rf2Parser.ConceptGroups)
      {
        RF2ConceptGroup rf2ConceptGroup = kvp.Value;
        SnomedQueryConcept concept = this.CreateConceptRecord(rf2ConceptGroup);
        if (concept != null)
          this.modelManager.Add(concept);
      }
    }

    /// <summary>
    /// Iterate over all rf2Parser concepts and create correct GcSnomedConcept derived class for each element.
    /// </summary>
    SnomedQueryConcept CreateConceptRecord(RF2ConceptGroup rf2ConceptGroup)
    {
      RF2Concept rf2Concept = rf2ConceptGroup.Active;
      if (rf2Concept == null)
        return null;

      RF2ConceptGroup module = this.rf2Parser.GetConceptGroup(rf2Concept.ModuleId);
      RF2ConceptGroup definitionStatus = this.rf2Parser.GetConceptGroup(rf2Concept.DefinitionStatusId);

      List<String> synonyms = new List<string>();
      foreach (RF2DescriptionGroup descriptionGroup in rf2ConceptGroup.DescriptionGroups)
      {
        RF2Description description = descriptionGroup.GetActiveEnglishDescription();
        switch (description.TypeId)
        {
          case RF2Parser.SynonymTypeId:
            synonyms.Add(description.Term);
            break;

          case RF2Parser.FullySpecifiedNameConceptId:
            break;

          default:
            Console.WriteLine($"Unimplemented description type {this.rf2Parser.GetConceptGroup(description.TypeId).GetFullySpecifiedName()}");
            break;
        }
      }

      SnomedQueryConcept concept = new SnomedQueryConcept(
        rf2Concept.Id,                                      // conceptId,
        rf2ConceptGroup.GetFullySpecifiedName(),            // conceptFullyQualifiedName,
        synonyms.ToArray(),                                 // conceptSynonymns,
        module.GetFullySpecifiedName(),                     // String conceptModule,
        definitionStatus.GetFullySpecifiedName(),           // String conceptDefinitionStatus,
        rf2Concept.EffectiveTime                            // conceptEffectiveTime
      );

      return concept;
    }
  }
}
