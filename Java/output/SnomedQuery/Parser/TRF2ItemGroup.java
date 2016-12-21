package SnomedQuery.Parser;

import java.util.*;

  /** 
   Base class for RF2 group classes.
   
   <typeparam name="T"></typeparam>
  */
//C# TO JAVA CONVERTER TODO TASK: The C# 'new()' constraint has no equivalent in Java:
//ORIGINAL LINE: public class TRF2ItemGroup<T> : RF2Item where T : RF2ItemSingle, new()
  public class TRF2ItemGroup<T extends RF2ItemSingle> extends RF2Item
  {
	/** 
	 Return curent active item. If current item is not active, return
	 null.
	*/
	public final T getActive()
	{
	  T item = this.getCurrent();
	  if (item.Active == false)
	  {
		return null;
	  }
	  return item;
	}

	/** 
	 Return item that is currently active.
	 Items can be added to snomed with an active date in the future.
	*/
	public final T getCurrent()
	{
	  int index = this.items.size() - 1;
	  while (index >= 0)
	  {
		T item = this.items.get(index);
		if (item.EffectiveTime.compareTo(java.time.LocalDateTime.now()) <= 0)
		{
		  return item;
		}
		index -= 1;
	  }
	  return null;
	}

	/** 
	 List of items in group.
	*/
	public final Iterable<T> getItems()
	{
		return this.items;
	}

	/** 
	 List of items in group.
	*/
	protected ArrayList<T> items = new ArrayList<T>();
  }