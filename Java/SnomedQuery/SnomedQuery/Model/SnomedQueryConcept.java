package SnomedQuery.Model;

import org.joda.time.DateTime;

/**
 * 
 * @author Travis Lukach
 *
 */
public class SnomedQueryConcept {
	/**
	 * <p>
	 * SNOMED concept ID
	 * </p>
	 */
	private long conceptId;

	/**
	 * <p>
	 * returns <b>conceptId</b>
	 * </p>
	 * 
	 * @return
	 */
	public long getConceptId() {
		return this.conceptId;
	}

	/**
	 * <p>
	 * SNOMED Fully Qualified Name
	 * </p>
	 */
	private String conceptFullyQualifiedName;

	/**
	 * <p>
	 * returns <b>conceptFullyQualifiedName</b>
	 * </p>
	 * 
	 * @return
	 */
	public String getConceptFullyQualifiedName() {
		return this.conceptFullyQualifiedName;
	}

	/**
	 * <p>
	 * SNOMED Name synonyms
	 * </p>
	 */
	private String[] conceptSynonyms;

	/**
	 * <p>
	 * returns <b>conceptSynonyms</b>
	 * </p>
	 * 
	 * @return
	 */
	public String[] getConceptSynonyms() {
		return this.conceptSynonyms;
	}

	/**
	 * <p>
	 * SNOMED Module
	 * </p>
	 */
	private String conceptModule;

	/**
	 * <p>
	 * returns <b>conceptModule</b>
	 * </p>
	 * 
	 * @return
	 */
	public String getConceptModule() {
		return this.conceptModule;
	}

	/**
	 * <p>
	 * SNOMED Definition Status
	 * </p>
	 */
	private String conceptDefinitionStatus;

	/**
	 * <p>
	 * returns <b>conceptDefinitionStatus</b>
	 * </p>
	 * 
	 * @return
	 */
	public String getConceptDefinitionStatus() {
		return this.conceptDefinitionStatus;
	}

	/**
	 * <p>
	 * SNOMED concept Effective Time
	 * </p>
	 */
	private DateTime conceptEffectiveTime;

	/**
	 * <p>
	 * returns <b>conceptEffectiveTime</b>
	 * </p>
	 * 
	 * @return
	 */
	public DateTime getConceptEffectiveTime() {
		return this.conceptEffectiveTime;
	}

	/**
	 * <p>
	 * SNOMED IsA concept parents
	 * </p>
	 */
	private SnomedQueryConcept[] isAParents = new SnomedQueryConcept[0];

	/**
	 * <p>
	 * returns <b>isAParents</b>
	 * </p>
	 * 
	 * @return
	 */
	public SnomedQueryConcept[] getIsAParents() {
		return this.isAParents;
	}

	/**
	 * <p>
	 * sets <b>isAParents</b>
	 * </p>
	 * 
	 * @param isAParents
	 */
	public void setIsAParents(final SnomedQueryConcept[] isAParents) {
		this.isAParents = isAParents;
	}

	/**
	 * <p>
	 * SNOMED isAChildren
	 * </p>
	 */
	private SnomedQueryConcept[] isAChildren = new SnomedQueryConcept[0];

	/**
	 * <p>
	 * returns <b>isAChildren</b>
	 * </p>
	 * 
	 * @return
	 */
	public SnomedQueryConcept[] getIsAChildren() {
		return this.isAChildren;
	}

	/**
	 * <p>
	 * sets isAChildren.
	 * </p>
	 * 
	 * @param isAChildren
	 */
	public void setIsAChildren(final SnomedQueryConcept[] isAChildren) {
		this.isAChildren = isAChildren;
	}

	/**
	 * <p>
	 * Class SnomedQueryConcept constructor
	 * </p>
	 * 
	 * @param conceptId
	 * @param conceptFullyQualifiedName
	 * @param conceptSynonyms
	 * @param conceptModule
	 * @param conceptDefinitionSatus
	 * @param conceptEffectiveTime
	 */
	public SnomedQueryConcept(final long conceptId, final String conceptFullyQualifiedName,
			final String[] conceptSynonyms, final String conceptModule,
			final String conceptDefinitionSatus, final DateTime conceptEffectiveTime) {
		this.conceptId = conceptId;
		this.conceptFullyQualifiedName = conceptFullyQualifiedName;
		this.conceptSynonyms = conceptSynonyms;
		this.conceptModule = conceptModule;
		this.conceptDefinitionStatus = conceptDefinitionSatus;
		this.conceptEffectiveTime = conceptEffectiveTime;
	}
}
