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
  /// Stored data from one line of the RFS concept file.
  /// </summary>
  public class RF2Relationship : RF2ItemSingle
  {
    /// <summary>
    /// Snomed Id of relationship source
    /// </summary>
    public Int64 SourceId;

    /// <summary>
    /// Snomed Id of relationship destination
    /// </summary>
    public Int64 DestinationId;

    /// <summary>
    /// Snomed relationship group.
    /// </summary>
    public Int32 RelationshipGroup;

    /// <summary>
    /// Snomed relationship type.
    /// </summary>
    public Int64 TypeId;

    /// <summary>
    /// Snomed relationship characteristic type id.
    /// </summary>
    public Int64 CharacteristicTypeId;

    /// <summary>
    /// Snomed modifier id.
    /// </summary>
    public Int64 ModifierId;

    /// <summary>
    /// Lazy load TypeConcept.
    /// </summary>
    public RF2ConceptGroup TypeConcept
    {
      get
      {
        if (this.typeConcept == null)
          this.typeConcept = this.Parser.GetConceptGroup(this.TypeId);
        return this.typeConcept;
      }
    }

    /// <summary>
    /// Backing field for TypeConcept property.
    /// </summary>
    RF2ConceptGroup typeConcept = null;

    /// <summary>
    /// Lazy load Source.
    /// </summary>
    public RF2ConceptGroup Source
    {
      get
      {
        if (this.source == null)
          this.source = this.Parser.GetConceptGroup(this.SourceId);
        return this.source;
      }
    }

    /// <summary>
    /// Backing field for Source property.
    /// </summary>
    RF2ConceptGroup source = null;

    /// <summary>
    /// Lazy load Destination.
    /// </summary>
    public RF2ConceptGroup Destination
    {
      get
      {
        if (this.destination == null)
          this.destination = this.Parser.GetConceptGroup(this.DestinationId);
        return this.destination;
      }
    }

    /// <summary>
    /// Backing field for Destination property.
    /// </summary>
    RF2ConceptGroup destination = null;

    /// <summary>
    /// Constructor
    /// </summary>
    public RF2Relationship()
    {
    }

    /// <summary>
    /// Parse single line from relationship rf2 file.
    /// </summary>
    /// <param name="parser"></param>
    /// <param name="line"></param>
    /// <returns></returns>
    public static RF2Relationship Parse(RF2Parser parser, String line)
    {
      String[] parts = line.Split('\t');
      if (parts.Length != 10)
        throw new ApplicationException("Invalid Relationship line");

      return new RF2Relationship()
      {
        Parser = parser,
        Id = Int64.Parse(parts[0]),
        EffectiveTime = ParseEffectiveTime(parts[1]),
        Active = ParseBool(parts[2]),
        ModuleId = Int64.Parse(parts[3]),
        SourceId = Int64.Parse(parts[4]),
        DestinationId = Int64.Parse(parts[5]),
        RelationshipGroup = Int32.Parse(parts[6]),
        TypeId = Int64.Parse(parts[7]),
        CharacteristicTypeId = Int64.Parse(parts[8]),
        ModifierId = Int64.Parse(parts[9])
      };
    }

    /// <summary>
    /// Override ToString method.
    /// </summary>
    /// <returns></returns>
    public override string ToString()
    {
      return $"RF2Relationship [{Id}] Active: {Active} TypeId:{TypeId} Source:{SourceId} Destination:{DestinationId}";
    }
  }
}
