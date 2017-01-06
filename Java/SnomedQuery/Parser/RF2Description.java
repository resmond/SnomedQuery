package Parser;

/**
 * <p>
 * Stored data from one line of the RFS concept file.
 * </p>
 * 
 * @author Travis Lukach
 */
public class RF2Description extends RF2ItemSingle {
	/**
	 * <p>
	 * Snomed concept id
	 * </p>
	 */
	private long conceptId;

	/**
	 * <p>
	 * Snomed language code.
	 * </p>
	 */
	private String languageCode;

	/**
	 * <p>
	 * Snomed description type id
	 * </p>
	 */
	private long typeId;

	/**
	 * <p>
	 * Snomed description Term
	 * </p>
	 */
	private String term;

	/**
	 * <p>
	 * Snomed description case significance id
	 * </p>
	 */
	private long caseSignificanceId;

	/**
	 * <p>
	 * Gets <i>conceptId</i>.
	 * </p>
	 * 
	 * @return
	 */
	public long getConceptId() {
		return conceptId;
	}

	/**
	 * <p>
	 * Sets <i>conceptId</i>.
	 * </p>
	 * 
	 * @param conceptId
	 */
	public void setConceptId(long conceptId) {
		this.conceptId = conceptId;
	}

	/**
	 * <p>
	 * Gets <i>languageCode</i>.
	 * </p>
	 * 
	 * @return
	 */
	public String getLanguageCode() {
		return languageCode;
	}

	/**
	 * <p>
	 * Sets <i>languageCode</i>.
	 * </p>
	 * 
	 * @param languageCode
	 */
	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	/**
	 * <p>
	 * Gets <i>typeId</i>.
	 * </p>
	 * 
	 * @return
	 */
	public long getTypeId() {
		return typeId;
	}

	/**
	 * <p>
	 * Sets <i>typeId</i>.
	 * </p>
	 * 
	 * @param typeId
	 */
	public void setTypeId(long typeId) {
		this.typeId = typeId;
	}

	/**
	 * <p>
	 * Gets <i>term</i>.
	 * </p>
	 * 
	 * @return
	 */
	public String getTerm() {
		return term;
	}

	/**
	 * <p>
	 * Sets <i>term</i>.
	 * </p>
	 * 
	 * @param term
	 */
	public void setTerm(String term) {
		this.term = term;
	}

	/**
	 * <p>
	 * Gets <i>caseSignificanceId</i>.
	 * </p>
	 * 
	 * @return
	 */
	public long getCaseSignificanceId() {
		return caseSignificanceId;
	}

	/**
	 * <p>
	 * Sets <i>caseSignificanceId</i>.
	 * </p>
	 * 
	 * @param caseSignificanceId
	 */
	public void setCaseSignificanceId(long caseSignificanceId) {
		this.caseSignificanceId = caseSignificanceId;
	}

	/**
	 * <p>
	 * Lazy load DescriptionTypeConcept.
	 * </p>
	 */
	public final RF2ConceptGroup getDescriptionTypeConcept() {
		if (this.descriptionTypeConcept == null) {
			this.descriptionTypeConcept = this.getParser()
					.getConceptGroup(this.conceptId);
		}
		return this.descriptionTypeConcept;
	}

	/**
	 * <p>
	 * Backing field for DescriptionTypeConcept property.
	 * </p>
	 */
	private RF2ConceptGroup descriptionTypeConcept = null;

	/**
	 * <p>
	 * Constructor.
	 * </p>
	 */
	public RF2Description() {
	}

	/**
	 * <p>
	 * Parse line of input into new RF2Description and return it.
	 * </p>
	 * 
	 * @param parser
	 * @param line
	 * @return
	 */
	public static RF2Description parse(RF2Parser parser, String line) {
		String[] parts = line.split("[\\t]");
		if (parts.length != 9) {
			throw new RuntimeException("Invalid Description line");
		}

		RF2Description tempVar = new RF2Description();
		tempVar.setParser(parser);
		tempVar.setId(Long.parseLong(parts[0]));
		tempVar.setEffectiveTime(parseEffectiveTime(parts[1]));
		tempVar.setActive(parseBool(parts[2]));
		tempVar.setModuleId(Long.parseLong(parts[3]));
		tempVar.conceptId = Long.parseLong(parts[4]);
		tempVar.setLanguageCode(parts[5]);
		tempVar.setTypeId(Long.parseLong(parts[6]));
		tempVar.setTerm(parts[7]);
		tempVar.setCaseSignificanceId(Long.parseLong(parts[8]));
		return tempVar;
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
		return "RF2Concept [{Id}] Active: {Active} {LanguageCode}-{Term}";
	}
}