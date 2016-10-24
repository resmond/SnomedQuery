using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Collections;
using System.ComponentModel;
using System.Diagnostics;
using System.IO;

namespace SnomedQuery.Model
{
  /// <summary>
  /// Snomed query concept.
  /// Holds all pertinaint info from snomed concept, including
  /// is a parents and children.
  /// </summary>
  public class SnomedQueryConcept
  {
    /// <summary>
    /// SNOMED concept ID
    /// </summary>
    public readonly Int64 ConceptId;

    /// <summary>
    /// SNOMED Fully Qualified Name
    /// </summary>
    public readonly String ConceptFullyQualifiedName;

    /// <summary>
    /// SNOMED Name synonymns
    /// </summary>
    public readonly String[] ConceptSynonymns;

    /// <summary>
    /// SNOMED Module
    /// </summary>
    public readonly String ConceptModule;

    /// <summary>
    /// SNOMED Definition Status
    /// </summary>
    public readonly String ConceptDefinitionStatus;


    /// <summary>
    /// SNOMED concept Effective Time
    /// </summary>
    public readonly DateTime ConceptEffectiveTime;

    /// <summary>
    /// SNOMED IsA concept parents
    /// </summary>
    public SnomedQueryConcept[] IsAParents = new SnomedQueryConcept[0];

    /// <summary>
    /// SNOMED IsA concept children
    /// </summary>
    public SnomedQueryConcept[] IsAChildren = new SnomedQueryConcept[0];

    /// <summary>
    /// Class SnomedQueryConcept constructor
    /// </summary>
    public SnomedQueryConcept(Int64 conceptId,
      String conceptFullyQualifiedName,
      String[] conceptSynonymns,
      String conceptModule,
      String conceptDefinitionStatus,
      DateTime conceptEffectiveTime)
    {
      this.ConceptId = conceptId;
      this.ConceptFullyQualifiedName = conceptFullyQualifiedName;
      this.ConceptSynonymns = conceptSynonymns;
      this.ConceptModule = conceptModule;
      this.ConceptDefinitionStatus = conceptDefinitionStatus;
      this.ConceptEffectiveTime = conceptEffectiveTime;
    }
  }
}
