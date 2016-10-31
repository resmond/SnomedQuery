package SnomedQuery.Model;

import org.joda.time.DateTime;

/**
 * <p>
 * Contains data and relationships between Snomed Concepts
 * </p>
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
	 * @return conceptId value
	 */
	public final long getConceptId() {
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
	 * @return conceptFullyQualifiedName value
	 */
	public final String getConceptFullyQualifiedName() {
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
	 * @return conceptSynonyms array.
	 */
	public final String[] getConceptSynonyms() {
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
	 * @return conceptModule value
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
	 * @return conceptDefinitionStatus value
	 */
	public final String getConceptDefinitionStatus() {
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
	 * @return conceptEffectiveTime value
	 */
	public final DateTime getConceptEffectiveTime() {
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
	 * @return isAParents value.
	 */
	public final SnomedQueryConcept[] getIsAParents() {
		return this.isAParents;
	}

	/**
	 * <p>
	 * sets <b>isAParents</b>
	 * </p>
	 * 
	 * @param isAParents
	 *            setting isAParents
	 */
	public final void setIsAParents(final SnomedQueryConcept[] isAParents) {
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
	 * @return isAChildren value
	 */
	public final SnomedQueryConcept[] getIsAChildren() {
		return this.isAChildren;
	}

	/**
	 * <p>
	 * sets isAChildren.
	 * </p>
	 * 
	 * @param isAChildren
	 *            value for setting is a children
	 */
	public final void setIsAChildren(final SnomedQueryConcept[] isAChildren) {
		this.isAChildren = isAChildren;
	}

	/**
	 * <p>
	 * Class SnomedQueryConcept constructor
	 * </p>
	 * 
	 * @param conceptId
	 *            to set conceptId
	 * @param conceptFullyQualifiedName
	 *            to set conceptFullyQualifiedName
	 * @param conceptSynonyms
	 *            to set conceptSynonyms
	 * @param conceptModule
	 *            to set conceptModule
	 * @param conceptDefinitionStatus
	 *            to set conceptDefinitionSatus
	 * @param conceptEffectiveTime
	 *            to set conceptEffectiveTime
	 */
	public SnomedQueryConcept(final long conceptId,
			final String conceptFullyQualifiedName,
			final String[] conceptSynonyms, final String conceptModule,
			final String conceptDefinitionStatus,
			final DateTime conceptEffectiveTime) {
		this.conceptId = conceptId;
		this.conceptFullyQualifiedName = conceptFullyQualifiedName;
		this.conceptSynonyms = conceptSynonyms;
		this.conceptModule = conceptModule;
		this.conceptDefinitionStatus = conceptDefinitionStatus;
		this.conceptEffectiveTime = conceptEffectiveTime;
	}
}
