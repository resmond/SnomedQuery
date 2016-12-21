package SnomedQuery.Parser;

  /** 
   Base class for RF@ single item classes.
  */
  public class RF2ItemSingle extends RF2Item
  {
	/** 
	 Snomed effective time that item is active
	*/
	public java.time.LocalDateTime EffectiveTime = java.time.LocalDateTime.MIN;

	/** 
	 Snomed active field.
	*/
	public boolean Active;

	/** 
	 Snomed module id
	*/
	public long ModuleId;
  }