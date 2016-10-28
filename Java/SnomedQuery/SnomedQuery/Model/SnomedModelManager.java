package SnomedQuery.Model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.LinkedList;
import org.joda.time.DateTime;

/**
 * <p>
 * SnomedModelManager class, handles data parsing and queries.
 * </p>
 * 
 * @author Travis Lukach
 *
 */
public class SnomedModelManager {

	/**
	 * <p>
	 * The serialized character set is UTF-8. Mainly used when creating new
	 * String objects.
	 * </p>
	 */
	public final String _charSet = "UTF-8";

	/**
	 * <p>
	 * Space String for validating deseralized String objects.
	 * </p>
	 */
	public final String _space = " ";

	/**
	 * <p>
	 * The null character for UTF-8, used for validating deserialized Strings.
	 * </p>
	 */
	public final char _UTF8Null = '�';

	/**
	 * <p>
	 * java.util.HashMap for holding a unique set of <b>SnomedQueryConcept</b>
	 * objects.
	 * </p>
	 */
	private HashMap<Long, SnomedQueryConcept> snomedConcepts = new HashMap<Long, SnomedQueryConcept>();

	/**
	 * <p>
	 * Base directory for data files when deserializing
	 * </p>
	 */
	private String baseDir;

	/**
	 * <p>
	 * java.nio.ByteBuffer wraps a byte array for reading and constructing
	 * <b>SnomedQueryConcept</b> object.
	 * </p>
	 */
	private ByteBuffer binaryReader;

	/**
	 * <p>
	 * Constructs a Path to the RawData directory.
	 * </p>
	 * 
	 * @return the RawData sub-directory of baseDir
	 */
	public String getRawDataDir() {
		return this.baseDir + "\\" + "RawData";
	}

	/**
	 * <p>
	 * Constructs a Path to the ParsedData directory.
	 * </p>
	 * 
	 * @return the ParsedData sub-directory of baseDir
	 */
	public String getParsedRecordsDir() {
		return this.baseDir + "\\" + "ParsedRecords";
	}

	/**
	 * <p>
	 * Sets the <b>baseDir</b> object with <i>baseDirParam</i> parameter.
	 * </p>
	 * 
	 * @param baseDirParam
	 *            sets the baseDir value
	 */
	public SnomedModelManager(String baseDirParam) {
		this.baseDir = baseDirParam;
	}

	/**
	 * <p>
	 * Loads the Snomed Concepts into memory.
	 * </p>
	 */
	public void loadRecords() {
		this.deserialize();
	}

	/**
	 * <p>
	 * Deserialize all records serialized in C#.
	 * </p>
	 */
	public void deserialize() {
		LinkedList<SnomedQueryConcept> list = new LinkedList<>();
		try {
			FileInputStream dataReader = new FileInputStream(
					new File(this.getParsedRecordsDir() + "\\"
							+ "SnomedQueryConcepts.Data.ser"));
			this.binaryReader = ByteBuffer
					.wrap(new byte[(int) dataReader.getChannel().size()]);
			dataReader.getChannel().read(this.binaryReader);
			this.binaryReader.position(0);
			this.binaryReader.order(ByteOrder.LITTLE_ENDIAN);
			int conceptCount = this.binaryReader.getInt();
			for (int i = 0; i < conceptCount; i++) {
				SnomedQueryConcept concept = this.deserializeConceptData();
				this.add(concept);
				list.addLast(concept);
			}
			dataReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			FileInputStream dataReader = new FileInputStream(
					new File(this.getParsedRecordsDir() + "\\"
							+ "SnomedQueryConcepts.IsARelationships.ser"));
			byte[] fileBytes = new byte[(int) dataReader.getChannel().size()];
			dataReader.read(fileBytes);
			this.binaryReader = ByteBuffer.wrap(fileBytes)
					.order(ByteOrder.LITTLE_ENDIAN);
			for (SnomedQueryConcept concept : list) {
				this.deserializeConceptRealtionship(concept);
			}
			dataReader.close();
		} catch (IOException e) {
			e.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * <p>
	 * Add snomed query concept to model manager.
	 * </p>
	 * 
	 * @param concept
	 *            the concept that will be inserted into <b>snomedConcepts</b>
	 * @throws Exception
	 *             general exception, custom message.
	 */
	public void add(SnomedQueryConcept concept) throws Exception {
		if (this.snomedConcepts.putIfAbsent(concept.getConceptId(),
				concept) != null)
			System.err.println(
					String.format("Error adding concept %s to dictionary.`",
							concept.getConceptId()));
	}

	SnomedQueryConcept deserializeConceptData() {

		SnomedQueryConcept concept = new SnomedQueryConcept(
				this.binaryReader.getLong(), // conceptId
				this.getString(), // qualifiedName
				this.getStringArray(), // Synonyms
				this.getString(), // conceptModule
				this.getString(), // conceptDefinitionSatus
				this.getDateTime()); // conceptEffectiveTime
		concept.setIsAParents(
				new SnomedQueryConcept[this.binaryReader.getInt()]);
		concept.setIsAChildren(
				new SnomedQueryConcept[this.binaryReader.getInt()]);
		return concept;
	}

	/**
	 * <p>
	 * gets the length of the char[] to be deserialized first, then deserializes
	 * the bytes before validating them.
	 * </p>
	 * 
	 * @return parsed and validated string created from byte[]
	 */
	String getString() {
		boolean invalid = true;
		int mod = 0;
		String retVal = new String();
		int length = this.binaryReader.getInt();

		byte[] valChars = new byte[length];
		for (int i = 0; i < length; i++) {
			valChars[i] = this.binaryReader.get();
		}

		try {
			retVal = new String(valChars, this._charSet);
			// Validation
			while (invalid) {
				if (retVal.length() >= length && !retVal.endsWith(this._space))
					invalid = false;
				if (!invalid)
					for (char c : retVal.toCharArray()) {
						invalid = true;
						if (Character.isDefined(c) && c != this._UTF8Null)
							invalid = false;
						if (invalid)
							break;
					}
				if (invalid) {
					mod++;
					byte[] extendBytes = new byte[length + mod];
					for (int i = 0; i < valChars.length; i++) {
						extendBytes[i] = valChars[i];
					}
					extendBytes[length + mod - 1] = this.binaryReader.get();
					valChars = extendBytes;
					retVal = new String(extendBytes, this._charSet);
				}
			}
		} catch (UnsupportedEncodingException e) {
			System.err.println(e.getMessage());
			return null;
		}
		return retVal;

	}

	/**
	 * <p>
	 * constructs a String array by reading th length of the String array first
	 * out of <b>binaryReader</b> before
	 * </p>
	 * 
	 * @return deserialized String array.
	 */
	String[] getStringArray() {
		int length = this.binaryReader.getInt();
		String[] retVal = new String[length];
		for (int i = 0; i < length; i++) {
			retVal[i] = getString();
		}
		return retVal;
	}

	/**
	 * <p>
	 * constructs a JodaTime DateTime object out of <b>binaryReader</b>.
	 * </p>
	 * 
	 * @return a new DateTime object.
	 */
	DateTime getDateTime() {
		return new DateTime(this.binaryReader.getLong());
	}

	/**
	 * <p>
	 * Link up parent/child relationships
	 * </p>
	 * 
	 * @param concept
	 *            concept to have relationships linked to it
	 */
	void deserializeConceptRealtionship(SnomedQueryConcept concept) {
		SnomedQueryConcept[] childConcepts = concept.getIsAChildren();
		for (int i = 0; i < childConcepts.length; i++) {
			long childConceptId = this.binaryReader.getLong();
			SnomedQueryConcept child = this.getConceptById(childConceptId);
			SnomedQueryConcept[] childParents = child.getIsAParents();
			childParents[this.binaryReader.getInt()] = concept;
			child.setIsAParents(childParents);
			childConcepts[i] = child;
			concept.setIsAChildren(childConcepts);
		}
	}

	/**
	 * <p>
	 * Get concept by its id. Return null if concept not found.
	 * </p>
	 * 
	 * @param id
	 *            searched
	 * @return the queried id. Null if not found.
	 */
	public SnomedQueryConcept getConceptById(long id) {
		SnomedQueryConcept retVal;
		retVal = this.snomedConcepts.get(id);
		return retVal;
	}

	/**
	 * <p>
	 * Return true if concept with child id is a child of parent.
	 * </p>
	 * 
	 * @param visitedItems
	 *            persisted list of items that have already been viewed
	 * @param parent
	 *            SnomedQueryConcept of which children is about to be parsed
	 * @param childId
	 *            target child Id that is being searched
	 * @return true if there is a child object with child id
	 */
	public boolean isChild(HashMap<Long, SnomedQueryConcept> visitedItems,
			SnomedQueryConcept parent, long childId) {
		SnomedQueryConcept[] children = parent.getIsAChildren();
		for (SnomedQueryConcept child : children) {
			if (!visitedItems.containsKey(child.getConceptId())) {
				visitedItems.put(child.getConceptId(), child);
				if (child.getConceptId() == childId)
					return true;
				if (this.isChild(visitedItems, child, childId) == true)
					return true;
			}
		}
		return false;
	}

	/**
	 * <p>
	 * Return true if parent is an ancestor of child
	 * </p>
	 * 
	 * @param parentId
	 *            parent Id
	 * @param childId
	 *            child Id
	 * @return true if there is a child ancestor
	 */
	public boolean isChild(long parentId, long childId) {
		SnomedQueryConcept parent = this.getConceptById(parentId);
		HashMap<Long, SnomedQueryConcept> visitedItems = new HashMap<>();
		return this.isChild(visitedItems, parent, childId);
	}

	/**
	 * <p>
	 * Returns true if the parent hand child have a direct relationship.
	 * </p>
	 * 
	 * @param parentId
	 *            target parent Id
	 * @param childId
	 *            target child Id
	 * @return true if the relationship is direct.
	 */
	public boolean isImmediateChild(long parentId, long childId) {
		SnomedQueryConcept parent = this.getConceptById(parentId);
		SnomedQueryConcept[] children = parent.getIsAChildren();
		for (SnomedQueryConcept child : children) {
			if (child.getConceptId() == childId)
				return true;

		}
		return false;
	}

	/**
	 * <p>
	 * Returns a long Array of SNOMED Id's from a target record id.
	 * </p>
	 * 
	 * @param parentId
	 *            target parent Id
	 * @return long array of SNOMED Id's, null if there are no child Id's.
	 */
	public long[] getChildrenIds(long parentId) {
		SnomedQueryConcept parent = this.getConceptById(parentId);
		SnomedQueryConcept[] children = parent.getIsAChildren();
		if (children.length == 0)
			return null;
		long[] retVal = new long[children.length];
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = children[i].getConceptId();
		}
		return retVal;
	}

	/**
	 * Collect children of parent and continue visitor pattern onward with
	 * children.
	 * 
	 * @param visitedItems
	 *            Items already visited
	 * @param parent
	 *            parent concept
	 * @return the map of visited items.
	 */
	public HashMap<Long, SnomedQueryConcept> findDecendents(
			HashMap<Long, SnomedQueryConcept> visitedItems,
			SnomedQueryConcept parent) {
		for (SnomedQueryConcept child : parent.getIsAChildren()) {
			if (!visitedItems.containsKey(child.getConceptId())) {
				visitedItems.put(child.getConceptId(), child);
				this.findDecendents(visitedItems, child);
			}
		}
		return visitedItems;
	}

	/**
	 * <p>
	 * Collect child IDs of parent and continue visitor pattern onward with
	 * children
	 * </p>
	 * 
	 * @param conceptId the queried Id
	 * @return The array of child concepts.
	 */
	public SnomedQueryConcept[] findDescendants(long conceptId) {
		SnomedQueryConcept concept = this.getConceptById(conceptId);
		HashMap<Long, SnomedQueryConcept> visitedItems = new HashMap<>();
		visitedItems = this.findDecendents(visitedItems, concept);
		SnomedQueryConcept[] retVal = new SnomedQueryConcept[visitedItems
				.size()];
		int i = 0;
		for (SnomedQueryConcept qc : visitedItems.values()) {
			retVal[i] = qc;
			i++;
		}
		return retVal;

	}

	/**
	 * <p>
	 * Recursive Method for gathering all parents, and further up the line.
	 * </p>
	 * 
	 * @param visitedItems hashMap of gathered items.
	 * @param child current child looking for parents.
	 * @return the collected HashMap of objects.
	 */
	public HashMap<Long, SnomedQueryConcept> findAncestors(
			HashMap<Long, SnomedQueryConcept> visitedItems,
			SnomedQueryConcept child) {
		for (SnomedQueryConcept parent : child.getIsAParents()) {
			if (!visitedItems.containsKey(parent.getConceptId())) {
				visitedItems.put(parent.getConceptId(), parent);
				this.findDecendents(visitedItems, parent);
			}
		}
		return visitedItems;
	}

	/**
	 * <p>
	 * The start of finding all the parent Id's from target Id
	 * </p>
	 * 
	 * @param conceptId
	 *            the targetId that is to be queried
	 * @return All the parent objects in an array.
	 */
	public SnomedQueryConcept[] findAncestors(long conceptId) {
		SnomedQueryConcept concept = this.getConceptById(conceptId);
		HashMap<Long, SnomedQueryConcept> visitedItems = new HashMap<>();
		visitedItems = this.findAncestors(visitedItems, concept);
		SnomedQueryConcept[] retVal = new SnomedQueryConcept[visitedItems
				.size()];
		int i = 0;
		for (SnomedQueryConcept qc : visitedItems.values()) {
			retVal[i] = qc;
			i++;
		}
		return retVal;

	}

}
