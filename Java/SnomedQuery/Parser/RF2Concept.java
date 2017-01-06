package Parser;

/**
 * <p>
 * Stored data from one line of the RFS concept file.
 * </p>
 * 
 * @author Travis Lukach
 */
public class RF2Concept extends RF2ItemSingle {
	/**
	 * <p>
	 * Snomed definition status concept id.
	 * </p>
	 */
	private long definitionStatusId;

	/**
	 * <p>
	 * gets <i>definitionStatusId</i>.
	 * </p>
	 * 
	 * @return
	 */
	public long getDefinitionStatusId() {
		return definitionStatusId;
	}

	/**
	 * sets <i>definitionStatusId</i>.
	 * @param definitionStatusId
	 */
	public void setDefinitionStatusId(long definitionStatusId) {
		this.definitionStatusId = definitionStatusId;
	}
	/**
	 * <p>
	 * Constructor.
	 * </p>
	 */
	public RF2Concept() {
	}

	/**
	 * Parse a single line of snomed concept rf2 file.
	 * 
	 * @param parser
	 * @param line
	 * @return
	 */
	public static RF2Concept parse(RF2Parser parser, String line) {
		String[] parts = line.split("[\\t]", -1);
		if (parts.length != 5) {
			throw new RuntimeException("Invalid concept line");
		}

		RF2Concept tempVar = new RF2Concept();
		tempVar.setParser(parser);
		tempVar.setId(Long.parseLong(parts[0]));
		tempVar.setEffectiveTime(parseEffectiveTime(parts[1]));
		tempVar.setActive(parseBool(parts[2]));
		tempVar.setModuleId(Long.parseLong(parts[3]));
		tempVar.setDefinitionStatusId(Long.parseLong(parts[4]));
		return tempVar;
	}

	/**
	 * ToString() overload.
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return String.format("RF2Concept [%1$s] Active: %2$s Module: %3$s", getId(),
				isActive(), getModuleId());
	}

}