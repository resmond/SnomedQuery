package SnomedQuery.JUnitTests;

import static org.junit.Assert.*;

import java.io.File;
import org.joda.time.DateTime;
import org.junit.Test;
import SnomedQuery.Model.SnomedModelManager;

/**
 * <p>
 * JUnit Test Class.
 * </p>
 *
 * @author Travis Lukach
 *
 */
public class SnomedQueryTests {

	/**
	 * <p>
	 * Creates the ModelManager object with a relative path being fed in.
	 * </p>
	 *
	 * @return instantiated SnomedModelManager
	 */
	final SnomedModelManager modelManager() {
		File file = new File("");
		String baseDir = file.getAbsolutePath() + "\\..\\..\\Data";
		SnomedModelManager modelManger = new SnomedModelManager(baseDir);
		DateTime timeStart = DateTime.now();
		modelManger.loadRecords();
		DateTime timeEnd = DateTime.now();
		DateTime combined = new DateTime(timeEnd.getMillis() - timeStart.getMillis());
		System.out.println(combined.getSecondOfMinute() + "." + combined.getMillisOfSecond());
		return modelManger;
	}

	/**
	 * <p>
	 * See if the IsChild methods work with current work
	 * </p>
	 */
	@Test
	public final void modelMangerIsChildTest() {
		final long snomedConceptId = 138875005;
		final long bodyStructure = 123037004;
		final long specimen = 123038009;
		final long clinicalFinding = 404684003;
		final long onExaminationJointSynovialThickening = 164527008;
		SnomedModelManager modelManager = this.modelManager();

		assertTrue(modelManager.isChild(snomedConceptId, bodyStructure));
		assertTrue(modelManager.isChild(snomedConceptId, specimen));
		assertTrue(modelManager.isChild(snomedConceptId, onExaminationJointSynovialThickening));
		assertTrue(modelManager.isChild(clinicalFinding, onExaminationJointSynovialThickening));
		assertFalse(modelManager.isChild(specimen, onExaminationJointSynovialThickening));
	}

	/**
	 * <p>
	 * Test the children closure method
	 * </p>
	 */
	@Test
	public final void modelManagerClosureChild() {
		final long snomedConceptId = 138875005;
		final long bodyStructure = 123037004;
		final long specimen = 123038009;
		final long clinicalFinding = 404684003;
		final long onExaminationJointSynovialThickening = 164527008;
		SnomedModelManager modelManager = this.modelManager();
		System.out.println(modelManager.findDescendants(snomedConceptId).length);
		System.out.println(modelManager.findDescendants(bodyStructure).length);
		System.out.println(modelManager.findDescendants(specimen).length);
		System.out.println(modelManager.findDescendants(clinicalFinding).length);
		System.out.println(modelManager.findDescendants(onExaminationJointSynovialThickening).length);

	}

	/**
	 * <p>
	 * Test the parent closer method
	 * </p>
	 */
	@Test
	public final void modelManagerClosureParent() {
		final long snomedConceptId = 138875005;
		final long bodyStructure = 123037004;
		final long specimen = 123038009;
		final long clinicalFinding = 404684003;
		final long onExaminationJointSynovialThickening = 164527008;
		SnomedModelManager modelManager = this.modelManager();
		System.out.println(modelManager.findAncestors(snomedConceptId).length);
		System.out.println(modelManager.findAncestors(bodyStructure).length);
		System.out.println(modelManager.findAncestors(specimen).length);
		System.out.println(modelManager.findAncestors(clinicalFinding).length);
		System.out.println(modelManager.findAncestors(onExaminationJointSynovialThickening).length);
	}
}
