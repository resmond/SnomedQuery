This is the root folder of the SnomedQuery code.
SnoMed is implemented in both C# and Java.
This code allows the caller to make queries against the Snomed database


C# - the following shows how to use this from c#. The calling program needs to
reference the SnomedQuery.Model.dll assembly.


const Int64 SnomedConceptId = 138875005;
const Int64 BodyStructure = 123037004;
const Int64 Specimen = 123038009;
const Int64 ClinicalFinding = 404684003;
const Int64 OnExaminationJointSynovialThickening = 164527008;

// Create model manager. Tell model manager where to find prepearsed Snomed records.
String baseDir = Path.Combine("..", "Data");
SnomedModelManager modelManger = new SnomedModelManager(baseDir);
modelManger.LoadRecords();

// Body structure is child of concept.
Assert.IsTrue(modelManger.IsChild(SnomedConceptId, BodyStructure));

// Body structure is child of specimen.
Assert.IsTrue(modelManger.IsChild(SnomedConceptId, Specimen));

// OnExaminationJointSynovialThickening is child of concept.
Assert.IsTrue(modelManger.IsChild(SnomedConceptId, OnExaminationJointSynovialThickening));

// OnExaminationJointSynovialThickening is child of finding.
Assert.IsTrue(modelManger.IsChild(ClinicalFinding, OnExaminationJointSynovialThickening));

// OnExaminationJointSynovialThickening is NOT child of specimen.
Assert.IsFalse(modelManger.IsChild(Specimen, OnExaminationJointSynovialThickening));


Java - the following shows how to use SnomedQuery from Java. The calling program needs to
reference org.joda.time for the DateTime object.  The calling program itself must
have SnomedQuery.jar included on the buildpath as well as joda-time.

final long snomedConceptId = 138875005;
final long bodyStructure = 123037004;
final long specimen = 123038009;
final long clinicalFinding = 404684003;
final long onExaminationJointSynovialThickening = 164527008;

// Create the model manager. Tell the constructor where to find the preparesed records.
SnomedModelManager modelManger = new SnomedModelManager("\\..\\..\\Data");
modelManager.loadRecords();

// Body structure is child of concept. Function returns 'true'.
modelManager.isChild(snomedConceptId, bodyStructure);

// Body structure is child of specimen. Function returns 'true'.
modelManager.isChild(snomedConceptId, specimen);

// OnExaminationJointSynovialThickening is child of concept. Function returns 'true'.
modelManager.isChild(snomedConceptId, onExaminationJointSynovialThickening);

// OnExaminationJointSynovialThickening is child of finding. Function returns 'true'.
modelManager.isChild(clinicalFinding, onExaminationJointSynovialThickening);

// OnExaminationJointSynovialThickening is NOT child of specimen. Function returns 'false'.
modelManager.isChild(specimen, onExaminationJointSynovialThickening);
