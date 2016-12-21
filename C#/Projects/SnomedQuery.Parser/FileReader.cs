using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SnomedQuery.Parser
{
  public class FileReader
  {
    public static StreamReader reader;
    
    public static bool OpenFile(String path)
    {
      try { 
        reader = new StreamReader(File.OpenRead(path));
        return true;
      } catch (Exception e) {
        return false;
      }
    }

    public static String ReadLine()
    {
      if (reader == null)
        return null;
      if (reader.EndOfStream)
        return null;
      else
      {
        return reader.ReadLine();
      }
    }

    public static void CloseFile()
    {
      if (reader == null)
        return;
      reader.Close();
      reader = null;
    }

  }
}
