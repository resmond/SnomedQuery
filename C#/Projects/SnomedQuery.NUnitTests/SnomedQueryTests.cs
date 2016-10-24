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
