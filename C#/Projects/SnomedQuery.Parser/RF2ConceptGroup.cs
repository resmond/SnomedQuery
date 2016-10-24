using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SnomedQuery.Parser
{
  /// <summary>
  /// Group of concepts, all with same SNOMED id.
  /// </summary>
  public class RF2ConceptGroup : TRF2ItemGroup<RF2Concept>
  {
    /// <summary>
    /// List of all snomed descriptions for this concept.
    /// </summary>
    List<Int64> descriptionGroupIds = new List<Int64>();

    /// <summary>
    /// List of all snomed attribute concepts in which this concept is the
    /// source concept.
    /// </summary>
    List<Int64> sourceRelationshipIds = new List<Int64>();

    /// <summary>
    /// List of all snomed attribute concepts in which this concept is the
    /// destination concept.
    /// </summary>
    List<Int64> destinationRelationshipIds = new List<Int64>();

    /// <summary>
    /// Chain of is a parents.
    /// </summary>
    public List<Int64> IsAParents = new List<Int64>();

    /// <summary>
    /// Concept id for the class that this snomed 
    /// is an instance of.
    /// </summary>
    public Int64 ClassConceptId = -1;

    /// <summary>
    /// Top level node that this concept group is descended from.
    /// </summary>
    public Int64 TopLevelConceptId = -1;

    /// <summary>
    /// Lazy load Description groups.
    /// </summary>
    public RF2DescriptionGroup[] DescriptionGroups
    {
      get
      {
        // # Tested.
        if (this.descriptionGroups == null)
        {
          RF2DescriptionGroup[] temp = new RF2DescriptionGroup[this.descriptionGroupIds.Count];
          for (Int32 i = 0; i < this.descriptionGroupIds.Count; i++)
            temp[i] = this.Parser.GetDescriptionGroup(this.descriptionGroupIds[i]);
          this.descriptionGroups = temp;
        }
        return this.descriptionGroups;
      }
    }

    /// <summary>
    /// Description groups
    /// </summary>
    RF2DescriptionGroup[] descriptionGroups = null;

    /// <summary>
    /// Lazy load Source relationship groups.
    /// </summary>
    public RF2RelationshipGroup[] SourceRelationships
    {
      get
      {
        // # Not Tested.
        if (this.sourceRelationshipGroups == null)
        {
          RF2RelationshipGroup[] temp = new RF2RelationshipGroup[this.sourceRelationshipIds.Count];
          for (Int32 i = 0; i < this.sourceRelationshipIds.Count; i++)
            temp[i] = this.Parser.GetRelationshipGroup(this.sourceRelationshipIds[i]);
          this.sourceRelationshipGroups = temp;
        }
        return this.sourceRelationshipGroups;
      }
    }

    /// <summary>
    /// Relationship groups in which this concept is the source.
    /// </summary>
    RF2RelationshipGroup[] sourceRelationshipGroups = null;

    /// <summary>
    /// Lazy load Destination relationship groups.
    /// </summary>
    public RF2RelationshipGroup[] DestinationRelationships
    {
      get
      {
        // # Tested.
        if (this.destinationRelationshipGroups == null)
        {
          RF2RelationshipGroup[] temp = new RF2RelationshipGroup[this.destinationRelationshipIds.Count];
          for (Int32 i = 0; i < this.destinationRelationshipIds.Count; i++)
            temp[i] = this.Parser.GetRelationshipGroup(this.destinationRelationshipIds[i]);
          this.destinationRelationshipGroups = temp;
        }
        return this.destinationRelationshipGroups;
      }
    }

    /// <summary>
    /// Relationship groups in which this concept is the destination.
    /// </summary>
    RF2RelationshipGroup[] destinationRelationshipGroups = null;

    /// <summary>
    /// Constructor
    /// </summary>
    public RF2ConceptGroup()
    {
    }

    /// <summary>
    /// Add concept to concept group.
    /// </summary>
    /// <param name="concept"></param>
    public void AddConcept(RF2Concept concept)
    {
      this.items.Add(concept);
    }

    /// <summary>
    /// Add description group to concept group.
    /// </summary>
    /// <param name="descriptionGroup"></param>
    public void AddDescriptionGroup(RF2DescriptionGroup descriptionGroup)
    {
      if (this.descriptionGroupIds.Contains(descriptionGroup.Id) == true)
        return;
      this.descriptionGroupIds.Add(descriptionGroup.Id);
      // force lazy reloading of description groups id called.
      this.descriptionGroups = null;
    }

    /// <summary>
    /// Add a source relationship to concept group.
    /// </summary>
    /// <param name="relationshipGroup">Relationship in which this instance is the source</param>
    public void AddSourceRelationship(RF2RelationshipGroup relationshipGroup)
    {
      if (this.sourceRelationshipIds.Contains(relationshipGroup.Id) == true)
        return;
      this.sourceRelationshipIds.Add(relationshipGroup.Id);

      // force lazy reloading of source relationship groups id called.
      this.sourceRelationshipGroups = null;
    }

    /// <summary>
    /// Add a destination relationship to concept group.
    /// </summary>
    /// <param name="relationshipGroup">Relationship in which this instance is the destination</param>
    public void AddDestinationRelationship(RF2RelationshipGroup relationshipGroup)
    {
      if (this.destinationRelationshipIds.Contains(relationshipGroup.Id) == true)
        return;
      this.destinationRelationshipIds.Add(relationshipGroup.Id);

      // force lazy reloading of source relationship groups id called.
      this.destinationRelationshipGroups = null;
    }

    /// <summary>
    /// Return Snomed fully qualified name.
    /// </summary>
    /// <returns></returns>
    public String GetFullySpecifiedName()
    {
      String fsnAny = String.Empty;

      foreach (RF2DescriptionGroup dg in this.DescriptionGroups)
      {
        foreach (RF2Description description in dg.Items)
        {
          if (description.TypeId == RF2Parser.FullySpecifiedNameConceptId)
          {
            if (description.LanguageCode == "en")
              return description.Term;
            fsnAny = description.Term;
          }
        }
      }

      return fsnAny;
    }

    /// <summary>
    /// Return list of all SNOMED relationship groups that are IsA relationships, where this concept is 
    /// the source.
    /// </summary>
    /// <returns></returns>
    public List<RF2RelationshipGroup> IsAChildRelationships()
    {
      List<RF2RelationshipGroup> retVal = new List<RF2RelationshipGroup>();

      foreach (RF2RelationshipGroup g in this.DestinationRelationships)
      {
        RF2Relationship relationship = g.Active;
        if (
          (relationship.Active == true) &&
          (relationship.TypeId == RF2Parser.IsAConceptId)
          )
        {
          retVal.Add(g);
        }
      }
      return retVal;
    }


    /// <summary>
    /// Override ToString method.
    /// </summary>
    /// <returns></returns>
    public override string ToString()
    {
      return $"RF2ConceptGroup [{Id}] {GetFullySpecifiedName()}";
    }
  }
}
