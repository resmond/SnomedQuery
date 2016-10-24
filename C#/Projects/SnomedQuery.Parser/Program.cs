using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SnomedQuery.Parser
{
  /// <summary>
  /// Main program for parser. Creates parser object and runs it.
  /// </summary>
  class Program
  {
    /// <summary>
    /// Main method of program.
    /// </summary>
    /// <param name="args"></param>
    static void Main(string[] args)
    {
      SnomedParser sp = new SnomedParser();
      sp.ParseAndSerialize();
    }
  }
}
