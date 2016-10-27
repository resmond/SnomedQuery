using NUnit.Framework;
using SnomedQuery.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;

namespace SnomedQuery.NUnitTests
{
  using System.IO;

  /// <summary>
  /// NUnit Test class.
  /// </summary>
  [TestFixture]
  public class TestClass
  {
    /// <summary>
    /// Crreate and initialize Snomed Model Manager instance.
    /// All the snomed records are loaded.
    /// </summary>
    /// <returns></returns>
    SnomedModelManager ModelManager()
    {
      String baseDir = Path.Combine(TestContext.CurrentContext.TestDirectory,
          "..",
          "..",
          "..",
          "Data");

      Assert.IsTrue(Directory.Exists(baseDir));
      SnomedModelManager modelManger = new SnomedModelManager(baseDir);
      modelManger.LoadRecords();
      return modelManger;
    }


    /// <summary>
    /// Test loading records.
    /// </summary>
    [Test]
    public void ModelManager_LoadRecordsTest()
    {
      SnomedModelManager modelManger = this.ModelManager();
    }

    [TestCase(91723000,// Anatomical structure (body structure), 3 Ancestors
      442083009, 123037004, 138875005)]
    [TestCase(278197002, // Entire respiratory system (body structure), 11 Ancestors
      20139000, 312419003, 91689009, 91723000, 442083009, 123037004,
      138875005, 698967008, 91722005, 362889002, 278195005)]
    [TestCase(54662009, // Green color (finding), 5 Ancestors
      107650008, 72724002, 441742003, 404684003, 138875005)]
    public void ModelManager_FindAncestors(Int64 conceptId, params Int64[] results)
    {
      SnomedModelManager modelManger = this.ModelManager();
      SnomedQueryConcept[] retVal = modelManger.FindAncestors(conceptId);
      Assert.IsTrue(retVal.Length == results.Length);

      foreach (SnomedQueryConcept concept in retVal)
      {
        Assert.IsTrue(results.Contains(concept.ConceptId));
      }
    }

    [TestCase(181216001, // Entire lung (body structure), 3 Decendents
      361982005, 361967000, 187543000)]
    [TestCase(133936004, // Adult (person), 6 Decendents
      105436006, 105437002, 105438007, 339947000, 224526002, 255409004)]
    [TestCase(208526001, // Closed fracture of head of femur(disorder), 7 Decendents
      703994007, 275338001, 208528000, 208529008, 208530003, 208531004, 704409006)]
    public void ModelManager_FindDecendents(Int64 conceptId, params Int64[] results)
    {
      SnomedModelManager modelManger = this.ModelManager();
      SnomedQueryConcept[] retVal = modelManger.FindDecendents(conceptId);
      Assert.IsTrue(retVal.Length == results.Length);

      foreach (SnomedQueryConcept concept in retVal)
      {
        Assert.IsTrue(results.Contains(concept.ConceptId));
      }
    }

    /// <summary>
    /// Test IsChild method with various known values.
    /// </summary>
    [Test]
    public void ModelManager_IsChildTest()
    {
      const Int64 SnomedConceptId = 138875005;
      const Int64 BodyStructure = 123037004;
      const Int64 Specimen = 123038009;
      const Int64 ClinicalFinding = 404684003;

      const Int64 OnExaminationJointSynovialThickening = 164527008;

      SnomedModelManager modelManger = this.ModelManager();

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
    }
  }
}
