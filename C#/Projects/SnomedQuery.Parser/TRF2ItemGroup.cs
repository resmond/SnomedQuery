using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SnomedQuery.Parser
{
  /// <summary>
  /// Base class for RF2 group classes.
  /// </summary>
  /// <typeparam name="T"></typeparam>
  public class TRF2ItemGroup<T> : RF2Item
    where T : RF2ItemSingle, new()
  {
    /// <summary>
    /// Return curent active item. If current item is not active, return
    /// null.
    /// </summary>
    public T Active
    {
      get
      {
        T item = this.Current;
        if (item.Active == false)
          return null;
        return item;
      }
    }

    /// <summary>
    /// Return item that is currently active.
    /// Items can be added to snomed with an active date in the future.
    /// </summary>
    public T Current
    {
      get
      {
        Int32 index = this.items.Count - 1;
        while (index >= 0)
        {
          T item = this.items[index];
          if (item.EffectiveTime <= DateTime.Now)
            return item;
          index -= 1;
        }
        return null;
      }
    }

    /// <summary>
    /// List of items in group.
    /// </summary>
    public IEnumerable<T> Items
    {
      get { return this.items; }
    }

    /// <summary>
    /// List of items in group.
    /// </summary>
    protected List<T> items = new List<T>();
  }
}
