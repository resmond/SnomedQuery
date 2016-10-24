using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SnomedQuery.Parser
{
  /// <summary>
  /// Group of relationships, all with same SNOMED id.
  /// </summary>
  public class RF2RelationshipGroup : TRF2ItemGroup<RF2Relationship>
  {
    /// <summary>
    /// Constructor
    /// </summary>
    public RF2RelationshipGroup()
    {
    }

    /// <summary>
    /// Add relationsip to relationship group.
    /// </summary>
    /// <param name="relationship"></param>
    public void AddRelationship(RF2Relationship relationship)
    {
      this.items.Add(relationship);
    }

    /// <summary>
    /// Override ToString() method.
    /// </summary>
    /// <returns></returns>
    public override string ToString()
    {
      StringBuilder sb = new StringBuilder();
      sb.Append($"RF2RelationshipGroup [{Id}] ");
      RF2Relationship active = this.items[0];
      sb.Append($"Type [{active.TypeId}] '{active.TypeConcept.GetFullySpecifiedName()}'");
      sb.Append($"Source [{active.SourceId}] '{active.Source.GetFullySpecifiedName()}'");
      sb.Append($"Dest [{active.DestinationId}] '{active.Destination.GetFullySpecifiedName()}'");

      return sb.ToString();
    }
  }
}
