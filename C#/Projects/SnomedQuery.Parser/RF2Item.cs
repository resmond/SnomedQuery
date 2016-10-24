using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SnomedQuery.Parser
{
  /// <summary>
  /// Base class for all RF2 items.
  /// </summary>
  public class RF2Item
  {
    /// <summary>
    /// Parser that created this node.
    /// </summary>
    public RF2Parser Parser;

    /// <summary>
    /// Snomed id.
    /// </summary>
    public Int64 Id;

    /// <summary>
    /// Parse string into boolean value.
    /// </summary>
    /// <param name="parser"></param>
    /// <param name="line"></param>
    /// <returns></returns>
    public static bool ParseBool(String s)
    {
      switch (s)
      {
        case "0": return false;
        case "1": return true;
        default: throw new NotImplementedException("Invalid boolean value");
      }
    }

    /// <summary>
    /// Parse effective time into c# date time instance.
    /// </summary>
    /// <param name="s"></param>
    /// <returns></returns>
    public static DateTime ParseEffectiveTime(String s)
    {
      if (s.Length != 8)
        throw new ApplicationException("Expected date of format YYYYMMDD");

      return new DateTime(Int32.Parse(s.Substring(0, 4)),
        Int32.Parse(s.Substring(4, 2)),
        Int32.Parse(s.Substring(6, 2)));
    }
  }
}
