using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SnomedQuery.Parser
{
  /// <summary>
  /// Base class for RF@ single item classes.
  /// </summary>
  public class RF2ItemSingle : RF2Item
  {
    /// <summary>
    /// Snomed effective time that item is active
    /// </summary>
    public DateTime EffectiveTime;

    /// <summary>
    /// Snomed active field.
    /// </summary>
    public bool Active;

    /// <summary>
    /// Snomed module id
    /// </summary>
    public Int64 ModuleId;
  }
}
