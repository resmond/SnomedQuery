package Parser;

/**
 * Stored data from one line of the RFS concept file.
 */
public class RF2Description extends RF2ItemSingle {
	/**
	 * Snomed concept id
	 */
	public long ConceptId;

	/**
	 * Snomed language code.
	 */
	public String LanguageCode;

	/**
	 * Snomed description type id
	 */
	public long TypeId;

	/**
	 * Snomed description Term
	 */
	public String Term;

	/**
	 * Snomed description case significance id
	 */
	public long CaseSignificanceId;

	/**
	 * Lazy load DescriptionTypeConcept.
	 */
	public final RF2ConceptGroup getDescriptionTypeConcept() {
		if (this.descriptionTypeConcept == null) {
			this.descriptionTypeConcept = this.Parser.GetConceptGroup(this.ConceptId);
		}
		return this.descriptionTypeConcept;
	}

	/**
	 * Backing field for DescriptionTypeConcept property.
	 */
	private RF2ConceptGroup descriptionTypeConcept = null;

	/**
	 * Constructor
	 */
	public RF2Description() {
	}

	/**
	 * Parse line of input into new RF2Description and return it.
	 * 
	 * @param parser
	 * @param line
	 * @return
	 */
	public static RF2Description Parse(RF2Parser parser, String line) {
		String[] parts = line.split("[\\t]");
		if (parts.length != 9) {
			throw new RuntimeException("Invalid Description line");
		}

		RF2Description tempVar = new RF2Description();
		tempVar.Parser = parser;
		tempVar.Id = Long.parseLong(parts[0]);
		tempVar.EffectiveTime = ParseEffectiveTime(parts[1]);
		tempVar.Active = ParseBool(parts[2]);
		tempVar.ModuleId = Long.parseLong(parts[3]);
		tempVar.ConceptId = Long.parseLong(parts[4]);
		tempVar.LanguageCode = parts[5];
		tempVar.TypeId = Long.parseLong(parts[6]);
		tempVar.Term = parts[7];
		tempVar.CaseSignificanceId = Long.parseLong(parts[8]);
		return tempVar;
	}

	/**
	 * Override ToString() method.
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return "RF2Concept [{Id}] Active: {Active} {LanguageCode}-{Term}";
	}
}