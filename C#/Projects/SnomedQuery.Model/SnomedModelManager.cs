using System;
using System.Collections.Generic;
using System.Collections.Concurrent;
using System.IO;
using System.Runtime;
using System.Threading;
using System.Threading.Tasks;

namespace SnomedQuery.Model
{
  /// <summary>
  /// Snomed Model Manager class.
  /// This class contains all the data that relates to the Snomed
  /// concepts. It loads preparsed data and allows specific
  /// queries against that data.
  /// </summary>
  public partial class SnomedModelManager
  {
    /// <summary>
    /// Dictionary of all concepts ordered by their snomed id.
    /// </summary>
    ConcurrentDictionary<Int64, SnomedQueryConcept> snomedConcepts = new ConcurrentDictionary<long, SnomedQueryConcept>();

    /// <summary>
    /// Base directory for data files
    /// </summary>
    String baseDir;

    /// <summary>
    /// Location of common raw files cache directory.
    /// </summary>
    public String RawDataDir
    {
      get { return Path.Combine(this.baseDir, "RawData"); }
    }

    /// <summary>
    /// Location of common parsed records cache directory.
    /// </summary>
    public String ParsedRecordsDir
    {
      get { return Path.Combine(this.baseDir, "ParsedRecords"); }
    }

    /// <summary>
    /// Constructor.
    /// </summary>
    /// <param name="baseDirParam">
    /// Base directory for data files
    /// The layout of the data MUST be
    /// {baseDirParam}
    ///   RawData (only used in parse)
    ///   ParsedRecords (preparsed records stored here)
    /// </param>
    public SnomedModelManager(String baseDirParam)
    {
      this.baseDir = baseDirParam;
    }

    /// <summary>
    /// Load records asynchronously
    /// </summary>
    /// <returns></returns>
    public void LoadRecords()
    {
      this.Deserialize();
    }

    /// <summary>
    /// Serialize all records created in this project.
    /// </summary>
    public void Serialize()
    {
      using (Stream dataStream = new FileStream(Path.Combine(this.ParsedRecordsDir, "SnomedQueryConcepts.Data.ser"),
                                                FileMode.Create,
                                                FileAccess.Write,
                                                FileShare.None),
            relationshipStream = new FileStream(Path.Combine(this.ParsedRecordsDir, "SnomedQueryConcepts.IsARelationships.ser"),
                                                FileMode.Create,
                                                FileAccess.Write,
                                                FileShare.None)
                                                )
      {
        using (BinaryWriter dataWriter = new BinaryWriter(dataStream),
              relationshipWriter = new BinaryWriter(relationshipStream))
        {
          this.Write(dataWriter, this.snomedConcepts.Count);      // write number of concepts
          // Write data part of each concept.
          foreach (SnomedQueryConcept snomedConcept in this.snomedConcepts.Values)
            this.SerializeConcept(dataWriter, relationshipWriter, snomedConcept);

          dataWriter.Flush();
          dataWriter.Close();
        }
        dataStream.Close();
      }
    }

    /// <summary>
    /// Serialize the data part of the snomed concept instance.
    /// </summary>
    /// <param name="dataWriter">Write conept data to this stream</param>
    /// <param name="relationshipWriter">Write IsA relationships to this stream</param>
    /// <param name="snomedConcept"></param>
    void SerializeConcept(BinaryWriter dataWriter,
      BinaryWriter relationshipWriter,
      SnomedQueryConcept snomedConcept)
    {
      this.Write(dataWriter, snomedConcept.ConceptId);
      this.Write(dataWriter, snomedConcept.ConceptFullyQualifiedName);
      this.Write(dataWriter, snomedConcept.ConceptSynonymns);
      this.Write(dataWriter, snomedConcept.ConceptModule);
      this.Write(dataWriter, snomedConcept.ConceptDefinitionStatus);
      this.Write(dataWriter, snomedConcept.ConceptEffectiveTime.Ticks);

      // Write out length of parent/child relationship arrays.
      this.Write(dataWriter, snomedConcept.IsAParents.Length);
      this.Write(dataWriter, snomedConcept.IsAChildren.Length);

      /*
       * This is a bit obtuse.
       * To avoid unnecessary dictionary lookups, we serialize the isa data as follows.
       * a) Only isa children are serialized. The parent fields are derived from the
       *    children values.
       * b) The children id are serialized, and the offset in the childs parent relationship
       *    array is serialized. This makes it possible to deserialice quickly, but serialization
       *    is very much slower.
       */
      // Output number of values in child list.
      for (Int32 i = 0; i < snomedConcept.IsAChildren.Length; i++)
      {
        SnomedQueryConcept child = snomedConcept.IsAChildren[i];
        this.Write(relationshipWriter, child.ConceptId);
        Int32 reverseIndex = -1;
        for (Int32 j = 0; j < child.IsAParents.Length; j++)
        {
          if (child.IsAParents[j] == snomedConcept)
          {
            reverseIndex = j;
            break;
          }
        }

        // If reverseIndex < 1, then the child record did not have a corresponding
        // link to the parent. This is an error.
        if (reverseIndex == -1)
          throw new ApplicationException("Reverse snomed index not found");
        this.Write(relationshipWriter, reverseIndex);
      }
    }

    /// <summary>
    /// Deserialize all records serialized in Serialize().
    /// </summary>
    public void Deserialize()
    {
      // Keep list of concepts read in, in the SAME ORDER they are read in.
      // This is used later to link up relationships.
      LinkedList<SnomedQueryConcept> list = new LinkedList<SnomedQueryConcept>();

      // Read in conept data. This does not read in relationships, though the
      // relationship arrays are resized properly.
      String dataPath = Path.Combine(this.ParsedRecordsDir, "SnomedQueryConcepts.Data.ser");
      dataPath = Path.GetFullPath(dataPath);
      using (Stream inStream = new FileStream(dataPath, FileMode.Open, FileAccess.Read, FileShare.Read))
      {
        using (BinaryReader dataReader = new BinaryReader(inStream))
        {
          Int32 conceptCount = this.ReadInt32(dataReader);
          for (Int32 i = 0; i < conceptCount; i++)
          {
            SnomedQueryConcept concept = this.DeserializeConceptData(dataReader);
            this.Add(concept);
            list.AddLast(concept);
          }
        }
      }

      // Read in relationship and link concepts togother. This must be done after all concepts
      // have been created so we can access the parent and child objects (cant point to object
      // that doesn't exist yet...)
      using (Stream inStream = new FileStream(
                             Path.Combine(this.ParsedRecordsDir, "SnomedQueryConcepts.IsARelationships.ser"),
                             FileMode.Open,
                             FileAccess.Read,
                             FileShare.Read))
      {
        using (BinaryReader relationshipReader = new BinaryReader(inStream))
        {
          foreach (SnomedQueryConcept concept in list)
          {
            this.DeserializeConceptRelationships(relationshipReader, concept);
          }
        }
      }
    }


    /// <summary>
    /// Add snomed query concept to model manager.
    /// </summary>
    /// <param name="concept"></param>
    public void Add(SnomedQueryConcept concept)
    {
      if (this.snomedConcepts.TryAdd(concept.ConceptId, concept) == false)
        throw new ApplicationException($"Error adding concept {concept.ConceptId} to dictionary.`");
    }

    /// <summary>
    /// Return snomed concept record with its data initialized from the serial input file.
    /// The is a links are not set yet.
    /// </summary>
    /// <param name="binaryReader"></param>
    /// <returns>Newly created snomed query concept instance</returns>
    SnomedQueryConcept DeserializeConceptData(BinaryReader binaryReader)
    {
      SnomedQueryConcept retVal = new SnomedQueryConcept(
              this.ReadInt64(binaryReader),                // conceptId
               this.ReadString(binaryReader),               // conceptFullyQualifiedName,
               this.ReadStringArray(binaryReader),          // conceptSynonymns,
               this.ReadString(binaryReader),               // conceptModule,
               this.ReadString(binaryReader),               // conceptDefinitionStatus,
               this.ReadDateTime(binaryReader)              // conceptEffectiveTime);
             );

      // Set length of parent/child arrays. The actual values in the array will be set later.
      {
        Int32 count = this.ReadInt32(binaryReader);
        retVal.IsAParents = new SnomedQueryConcept[count];
      }

      {
        Int32 count = this.ReadInt32(binaryReader);
        retVal.IsAChildren = new SnomedQueryConcept[count];
      }
      return retVal;
    }

    /// <summary>
    /// Link up parent/child relationships
    /// </summary>
    /// <param name="relationshipReader">Read relationship data from this stream</param>
    /// <param name="concept">Concept to set child relationship data for</param>
    /// <returns>Newly created snomed query concept instance</returns>
    void DeserializeConceptRelationships(BinaryReader relationshipReader,
      SnomedQueryConcept concept)
    {
      for (Int32 i = 0; i < concept.IsAChildren.Length; i++)
      {
        Int64 childConceptId = this.ReadInt64(relationshipReader);
        SnomedQueryConcept child = this.GetConceptById(childConceptId);
        concept.IsAChildren[i] = child;
        child.IsAParents[this.ReadInt32(relationshipReader)] = concept;
      }
    }

    /// <summary>
    /// Get concept by its id. Return null if concept not found.
    /// </summary>
    /// <param name="id"></param>
    /// <returns></returns>
    public SnomedQueryConcept GetConceptById(Int64 id)
    {
      SnomedQueryConcept retVal;
      if (this.snomedConcepts.TryGetValue(id, out retVal) == false)
        return null;
      return retVal;
    }

    /// <summary>
    /// Return true if concept with child id is a child of parent.
    /// </summary>
    /// <param name="visitedItems">Items already visited. Dont go there again</param>
    /// <param name="parent">parent concept</param>
    /// <param name="childID">child concept id to search for</param>
    /// <returns></returns>
    bool IsChild(ConcurrentDictionary<Int64, SnomedQueryConcept> visitedItems,
      SnomedQueryConcept parent,
      Int64 childID)
    {
      foreach (SnomedQueryConcept child in parent.IsAChildren)
      {
        // If we have not visited this before.
        if (visitedItems.TryAdd(child.ConceptId, child) == true)
        {
          if (child.ConceptId == childID)
            return true;

          if (this.IsChild(visitedItems, child, childID) == true)
            return true;
        }
      }
      return false;
    }

    /// <summary>
    /// Return true if parent is an ancestor of child
    /// </summary>
    /// <param name="parentID"></param>
    /// <param name="childID"></param>
    /// <returns></returns>
    public bool IsChild(Int64 parentID, Int64 childID)
    {
      SnomedQueryConcept parent = this.GetConceptById(parentID);
      ConcurrentDictionary<Int64, SnomedQueryConcept> visitedItems = new ConcurrentDictionary<long, SnomedQueryConcept>();
      return this.IsChild(visitedItems, parent, childID);
    }

    /// <summary>
    /// Collect child IDs of parent and continue visitor pattern onward with children.
    /// </summary>
    /// <param name="visitedItems">Items already visited. Dont go there again</param>
    /// <param name="decendents">All IDs of children visited from public FindDecendents.</param>
    /// <param name="parent">parent concept</param>
    /// <returns></returns>
    void FindDecendents(ConcurrentDictionary<Int64, SnomedQueryConcept> visitedItems,
       SnomedQueryConcept thisConcept)
    {
      foreach (SnomedQueryConcept child in thisConcept.IsAChildren)
      {
        // If we have not visited this before.
        if (visitedItems.TryAdd(child.ConceptId, child) == true)
        {
          this.FindDecendents(visitedItems, child);
        }
      }
    }

    /// <summary>
    /// Collect child IDs of parent and continue visitor pattern onward with children.
    /// </summary>
    /// <param name="visitedItems">Items already visited. Dont go there again</param>
    /// <param name="ancestors">All IDs of parents visited from public FindAncestors.</param>
    /// <param name="parent">parent concept</param>
    /// <returns></returns>
    void FindAncestors(ConcurrentDictionary<Int64, SnomedQueryConcept> visitedItems,
       SnomedQueryConcept thisConcept)
    {
      foreach (SnomedQueryConcept parent in thisConcept.IsAParents)
      {
        // If we have not visited this before.
        if (visitedItems.TryAdd(parent.ConceptId, parent) == true)
        {
          this.FindAncestors(visitedItems, parent);
        }
      }
    }

    /// <summary>
    /// Return Ancestors concepts of parent, does not include parent.
    /// </summary>
    /// <param name="parentID"></param>
    /// <returns></returns>
    public SnomedQueryConcept[] FindAncestors(Int64 currentId)
    {
      SnomedQueryConcept currentConcept = this.GetConceptById(currentId);
      ConcurrentDictionary<Int64, SnomedQueryConcept> ancestorIDs = new ConcurrentDictionary<Int64, SnomedQueryConcept>();
      this.FindAncestors(ancestorIDs, currentConcept);

      SnomedQueryConcept[] retVal = new SnomedQueryConcept[ancestorIDs.Count];
      ancestorIDs.Values.CopyTo(retVal, 0);
      return retVal;
    }

    /// <summary>
    /// Return Descendant concepts of parent, does not include parent.
    /// </summary>
    /// <param name="parentID"></param>
    /// <returns></returns>
    public SnomedQueryConcept[] FindDecendents(Int64 currentId)
    {
      SnomedQueryConcept currentConcept = this.GetConceptById(currentId);
      ConcurrentDictionary<Int64, SnomedQueryConcept> decendentIDs = new ConcurrentDictionary<Int64, SnomedQueryConcept>();
      this.FindDecendents(decendentIDs, currentConcept);
      SnomedQueryConcept[] retVal = new SnomedQueryConcept[decendentIDs.Count];
      decendentIDs.Values.CopyTo(retVal, 0);
      return retVal;
    }


    /// <summary>
    /// Outputs a Tab delimeted closure table file to the directory of choice.
    /// </summary>
    /// <param name="outputFile"></param>
    public void CreateClosureTable(String outputFile)
    {
      List<long[]> outputSet = new List<long[]>();
      long currentSource;
      long currentDestination;
      using (StreamWriter sWriter = new StreamWriter(outputFile))
      {
        foreach (SnomedQueryConcept concept in this.snomedConcepts.Values)
        {
          currentSource = concept.ConceptId;
          SnomedQueryConcept[] conceptArray = this.FindDecendents(concept.ConceptId);
          foreach (SnomedQueryConcept descendant in conceptArray)
          {
            currentDestination = descendant.ConceptId;
            outputSet.Add(new long[] { currentSource, currentDestination });
          }
        }
        foreach (long[] pair in outputSet)
        {
          sWriter.WriteLine(String.Format("{0}\t{1}", pair[0], pair[1]));
        }
      }
    }
  }
}