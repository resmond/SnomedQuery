package SnomedQuery.JUnitTests;

import java.io.File;

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
	 * 
	 * @return
	 */
	final SnomedModelManager modelManager() {
		File file = new File("");
		String baseDir = file.getAbsolutePath() + "\\..\\..\\Data";
		SnomedModelManager modelManger = new SnomedModelManager(baseDir);
		modelManger.loadRecords();
		return modelManger;
	}

	/**
	 * <p>
	 * See if the IsChild methods work with current work
	 * </p>
	 */
	@Test
	public final void modelManger_IsChildTest() {
		final long snomedConceptId = 138875005;
		final long bodyStructure = 123037004;
		final long specimen = 123038009;
		final long clinicalFinding = 404684003;
		final long onExaminationJointSynovialThickening = 164527008;

		SnomedModelManager modelManager = this.modelManager();

		System.out
				.println(modelManager.isChild(snomedConceptId, bodyStructure));
		System.out.println(modelManager.isChild(snomedConceptId, specimen));
		System.out.println(modelManager.isChild(snomedConceptId,
				onExaminationJointSynovialThickening));
		System.out.println(modelManager.isChild(clinicalFinding,
				onExaminationJointSynovialThickening));
		System.out.println(modelManager.isChild(specimen,
				onExaminationJointSynovialThickening));

	}
}