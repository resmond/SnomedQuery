﻿using System;
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
  public class RF2Concept : RF2ItemSingle
  {
    /// <summary>
    /// Snomed definition status concept id.
    /// </summary>
    public Int64 DefinitionStatusId;

    /// <summary>
    /// Constructor.
    /// </summary>
    public RF2Concept()
    {
    }

    /// <summary>
    /// Parse a single line of snomed concept rf2 file.
    /// </summary>
    /// <param name="parser"></param>
    /// <param name="line"></param>
    /// <returns></returns>
    public static RF2Concept Parse(RF2Parser parser, String line)
    {
      String[] parts = line.Split('\t');
      if (parts.Length != 5)
        throw new ApplicationException("Invalid concept line");

      return new RF2Concept()
      {
        Parser = parser,
        Id = Int64.Parse(parts[0]),
        EffectiveTime = ParseEffectiveTime(parts[1]),
        Active = ParseBool(parts[2]),
        ModuleId = Int64.Parse(parts[3]),
        DefinitionStatusId = Int64.Parse(parts[4])
      };
    }

    /// <summary>
    /// ToString() overload.
    /// </summary>
    /// <returns></returns>
    public override string ToString()
    {
      return $"RF2Concept [{Id}] Active: {Active} Module: {ModuleId}";
    }
  }
}
