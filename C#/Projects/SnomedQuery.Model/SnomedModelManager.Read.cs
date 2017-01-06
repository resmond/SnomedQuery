using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SnomedQuery.Model
{
  /// <summary>
  /// SnomedModelManager methods to read from BinaryReader
  /// </summary>
  public partial class SnomedModelManager
  {
    /// <summary>
    /// Read Int64.
    /// </summary>
    public Int64 ReadInt64(BinaryReader br)
    {
      return br.ReadInt64();
    }

    /// <summary>
    /// Read Int32 .
    /// </summary>
    public Int32 ReadInt32(BinaryReader br)
    {
      return br.ReadInt32();
    }

    /// <summary>
    /// Read DateTime.
    /// </summary>
    public DateTime ReadDateTime(BinaryReader br)
    {
      return new DateTime(br.ReadInt64());
    }

    /// <summary>
    /// Read string and return it.
    /// </summary>
    public String ReadString(BinaryReader br)
    {
      Int32 charCount = br.ReadInt32();
      char[] charsRead = new char[charCount];
      byte[] byteChar = new byte[2];
      for (int i = 0; i < charCount; i++)
      {
        byteChar = br.ReadBytes(2);
        charsRead[i] = BitConverter.ToChar(byteChar, 0);
      }
      return charsRead.ToString();
    }

    /// <summary>
    /// Read string array and return it.
    /// </summary>
    public String[] ReadStringArray(BinaryReader br)
    {
      Int32 stringCount = br.ReadInt32();
      String[] retVal = new string[stringCount];
      for (Int32 i = 0; i < stringCount; i++)
        retVal[i] = this.ReadString(br);
      return retVal;
    }

    /// <summary>
    /// Read Int64 array and return it.
    /// </summary>
    public Int64[] ReadInt64Array(BinaryReader br)
    {
      Int32 count = br.ReadInt32();
      Int64[] retVal = new Int64[count];
      for (Int32 i = 0; i < br.ReadInt32(); i++)
        retVal[i] = br.ReadInt64();
      return retVal;
    }
  }
}
