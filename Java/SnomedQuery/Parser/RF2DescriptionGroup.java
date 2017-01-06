package Parser;

/**
 * <p>
 * Group of descriptions, all with same SNOMED id.
 * </p>
 * 
 * @author Travis Lukach
 */
public class RF2DescriptionGroup extends TRF2ItemGroup<RF2Description> {
	/**
	 * <p>
	 * Constructor.
	 * </p>
	 */
	public RF2DescriptionGroup() {
	}

	/**
	 * <p>
	 * Add RF2Description to description group.
	 * </p>
	 * 
	 * @param description
	 */
	public final void addDescription(RF2Description description) {
		this.items.add(description);
	}

	/**
	 * <p>
	 * Get active description. Try to get english, but take anyone if english
	 * not found.
	 * </p>
	 * 
	 * @return
	 */
	public final RF2Description getActiveEnglishDescription() {
		RF2Description desciptionAny = null;
		for (RF2Description description : this.getItems()) {
			if (description.getLanguageCode().equals("en")) {
				return description;
			}
			desciptionAny = description;
		}
		return desciptionAny;
	}

	/**
	 * <p>
	 * Override ToString() method.
	 * </p>
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return "RF2DescriptionGroup [{Id}] Descriptions Count: {descriptions.Count}";
	}
}