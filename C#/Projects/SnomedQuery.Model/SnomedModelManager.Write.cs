using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SnomedQuery.Model
{
  /// <summary>
  /// SnomedModelManager methods to write data to a BinaryWriter
  /// </summary>
  public partial class SnomedModelManager
  {
    /// <summary>
    /// Write Int64.
    /// </summary>
    public void Write(BinaryWriter bw, Int64 value)
    {
      bw.Write(value);
    }

    /// <summary>
    /// Write Int32.
    /// </summary>
    /// <param name="bw"></param>
    /// <returns></returns>
    public void Write(BinaryWriter bw, Int32 value)
    {
      bw.Write(value);
    }

    /// <summary>
    /// Write DateTime.
    /// </summary>
    public void Write(BinaryWriter bw, DateTime value)
    {
      bw.Write(value.Ticks);
    }

    /// <summary>
    /// Write string array.
    /// </summary>
    public void Write(BinaryWriter bw,
      String[] value)
    {
      bw.Write(value.Length);
      for (Int32 i = 0; i < value.Length; i++)
        this.Write(bw, value[i]);
    }

    /// <summary>
    /// Write Int64 array.
    /// </summary>
    public void Write(BinaryWriter bw,
      Int64[] value)
    {
      bw.Write(value.Length);
      for (Int32 i = 0; i < value.Length; i++)
        bw.Write(value[i]);
    }

    /// <summary>
    /// Write String.
    /// </summary>
    public void Write(BinaryWriter bw,
      String value)
    {
      bw.Write(value.Length);
      for (int i = 0; i < value.Length; i++)
      {
        byte[] charByte = BitConverter.GetBytes(value.ToCharArray()[i]);
        bw.Write(charByte);
      }
    }
  }
}
