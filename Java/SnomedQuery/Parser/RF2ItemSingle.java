package Parser;

import org.joda.time.DateTime;

/** 
   Base class for RF@ single item classes.
  */
  public class RF2ItemSingle extends RF2Item
  {
	/** 
	 Snomed effective time that item is active
	*/
	public DateTime EffectiveTime = DateTime.now();
	/** 
	 Snomed active field.
	*/
	public boolean Active;

	/** 
	 Snomed module id
	*/
	public long ModuleId;
  }