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
  /// Group of descriptions, all with same SNOMED id.
  /// </summary>
  public class RF2DescriptionGroup : TRF2ItemGroup<RF2Description>
  {
    /// <summary>
    /// Constructor
    /// </summary>
    public RF2DescriptionGroup()
    {
    }

    /// <summary>
    /// Add RF2Description to description group.
    /// </summary>
    /// <param name="description"></param>
    public void AddDescription(RF2Description description)
    {
      this.items.Add(description);
    }

    /// <summary>
    /// Get active description. Try to get english, but take anyone if english
    /// not found.
    /// </summary>
    /// <returns></returns>
    public RF2Description GetActiveEnglishDescription()
    {
      RF2Description desciptionAny = null;
      foreach (RF2Description description in this.Items)
      {
        if (description.LanguageCode == "en")
          return description;
        desciptionAny = description;
      }
      return desciptionAny;
    }

    /// <summary>
    /// Override ToString() method.
    /// </summary>
    /// <returns></returns>
    public override string ToString()
    {
      return "RF2DescriptionGroup [{Id}] Descriptions Count: {descriptions.Count}";
    }
  }
}
