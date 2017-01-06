package Parser;

import org.joda.time.DateTime;

/**
 * <p>
 * Base class for RF@ single item classes.
 * </p>
 * 
 * @author Travis Lukach
 */
public class RF2ItemSingle extends RF2Item {
	/**
	 * <p>
	 * Snomed effective time that item is active
	 * </p>
	 */
	private DateTime effectiveTime = DateTime.now();
	/**
	 * <p>
	 * Snomed active field.
	 * </p>
	 */
	private boolean active;

	/**
	 * <p>
	 * Snomed module id
	 * </p>
	 */
	private long moduleId;

	/**
	 * <p>
	 * Gets <i>effectiveTime</i>.
	 * </p>
	 * 
	 * @return
	 */
	public DateTime getEffectiveTime() {
		return effectiveTime;
	}

	/**
	 * <p>
	 * Sets <i>effectiveTime</i>.
	 * </p>
	 * 
	 * @param effectiveTime
	 */
	public void setEffectiveTime(DateTime effectiveTime) {
		this.effectiveTime = effectiveTime;
	}

	/**
	 * <p>
	 * Gets <i>active</i>.
	 * </p>
	 * 
	 * @return
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * <p>
	 * Sets <i>active</i>.
	 * </p>
	 * 
	 * @param active
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * <p>
	 * Gets <i>moduleId</i>.
	 * </p>
	 * 
	 * @return
	 */
	public long getModuleId() {
		return moduleId;
	}

	/**
	 * <p>
	 * Sets <i>moduleId</i>.
	 * </p>
	 * 
	 * @param moduleId
	 */
	public void setModuleId(long moduleId) {
		this.moduleId = moduleId;
	}
}