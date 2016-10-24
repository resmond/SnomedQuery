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
  public class RF2Description : RF2ItemSingle
  {
    /// <summary>
    /// Snomed concept id
    /// </summary>
    public Int64 ConceptId;

    /// <summary>
    /// Snomed language code.
    /// </summary>
    public String LanguageCode;

    /// <summary>
    /// Snomed description type id
    /// </summary>
    public Int64 TypeId;

    /// <summary>
    /// Snomed description Term
    /// </summary>
    public String Term;

    /// <summary>
    /// Snomed description case significance id
    /// </summary>
    public Int64 CaseSignificanceId;


    /// <summary>
    /// Lazy load DescriptionTypeConcept.
    /// </summary>
    public RF2ConceptGroup DescriptionTypeConcept
    {
      get
      {
        if (this.descriptionTypeConcept == null)
          this.descriptionTypeConcept = this.Parser.GetConceptGroup(this.ConceptId);
        return this.descriptionTypeConcept;
      }
    }

    /// <summary>
    /// Backing field for DescriptionTypeConcept property.
    /// </summary>
    RF2ConceptGroup descriptionTypeConcept = null;

    /// <summary>
    /// Constructor
    /// </summary>
    public RF2Description()
    {
    }

    /// <summary>
    /// Parse line of input into new RF2Description and return it.
    /// </summary>
    /// <param name="parser"></param>
    /// <param name="line"></param>
    /// <returns></returns>
    public static RF2Description Parse(RF2Parser parser, String line)
    {
      String[] parts = line.Split('\t');
      if (parts.Length != 9)
        throw new ApplicationException("Invalid Description line");

      return new RF2Description()
      {
        Parser = parser,
        Id = Int64.Parse(parts[0]),
        EffectiveTime = ParseEffectiveTime(parts[1]),
        Active = ParseBool(parts[2]),
        ModuleId = Int64.Parse(parts[3]),
        ConceptId = Int64.Parse(parts[4]),
        LanguageCode = parts[5],
        TypeId = Int64.Parse(parts[6]),
        Term = parts[7],
        CaseSignificanceId = Int64.Parse(parts[8])
      };
    }

    /// <summary>
    /// Override ToString() method.
    /// </summary>
    /// <returns></returns>
    public override string ToString()
    {
      return "RF2Concept [{Id}] Active: {Active} {LanguageCode}-{Term}";
    }
  }
}
