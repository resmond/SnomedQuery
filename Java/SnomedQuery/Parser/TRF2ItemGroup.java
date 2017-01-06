package Parser;

import java.util.*;

import org.joda.time.DateTime;

/**
 * <p>
 * Base class for RF2 group classes.
 * </p>
 * 
 * @author Travis Lukach
 *
 * @param <T>
 */
public class TRF2ItemGroup<T extends RF2ItemSingle> extends RF2Item {
	/**
	 * <p>
	 * Return curent active item. If current item is not active, return null.
	 * </p>
	 * 
	 * @return
	 */
	public final T getActive() {
		T item = this.getCurrent();
		if (item.isActive() == false) {
			return null;
		}
		return item;
	}

	/**
	 * <p>
	 * Return item that is currently active. Items can be added to snomed with
	 * an active date in the future.
	 * </p>
	 * 
	 * @return
	 */
	public final T getCurrent() {
		int index = this.items.size() - 1;
		while (index >= 0) {
			T item = this.items.get(index);
			if (item.getEffectiveTime().compareTo(DateTime.now()) <= 0) {
				return item;
			}
			index -= 1;
		}
		return null;
	}

	/**
	 * <p>
	 * List of items in group.
	 * </p>
	 * 
	 * @return
	 */
	public final Iterable<T> getItems() {
		return this.items;
	}

	/**
	 * <p>
	 * List of items in group.
	 * </p>
	 */
	protected ArrayList<T> items = new ArrayList<T>();
}