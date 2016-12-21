package Parser;

/**
 * Stored data from one line of the RFS concept file.
 */
public class RF2Concept extends RF2ItemSingle {
	/**
	 * Snomed definition status concept id.
	 */
	public long DefinitionStatusId;

	/**
	 * Constructor.
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
	public static RF2Concept Parse(RF2Parser parser, String line) {
		String[] parts = line.split("[\\t]", -1);
		if (parts.length != 5) {
			throw new RuntimeException("Invalid concept line");
		}

		RF2Concept tempVar = new RF2Concept();
		tempVar.Parser = parser;
		tempVar.Id = Long.parseLong(parts[0]);
		tempVar.EffectiveTime = ParseEffectiveTime(parts[1]);
		tempVar.Active = ParseBool(parts[2]);
		tempVar.ModuleId = Long.parseLong(parts[3]);
		tempVar.DefinitionStatusId = Long.parseLong(parts[4]);
		return tempVar;
	}

	/**
	 * ToString() overload.
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return String.format("RF2Concept [%1$s] Active: %2$s Module: %3$s", Id, Active, ModuleId);
	}
}