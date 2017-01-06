package SnomedQuery.Model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
	public final char _UTF8Null = '\u00bd';

	/**
	 * <p>
	 * java.nio.ByteBuffer wraps a byte array for serializing
	 * <b>SnomedQueryConcept</b> concepts.
	 * </p>
	 */
	private ByteArrayOutputStream binaryWriter;

	/**
	 * <p>
	 * java.nio.ByteBuffer wraps a byte array for serailizing
	 * <b>SnomedQueryConcept</b> relationships.
	 * </p>
	 */
	private ByteArrayOutputStream relationshipWriter;

	/**
	 * <p>
	 * java.util.HashMap for holding a unique set of <b>SnomedQueryConcept</b>
	 * objects.
	 * </p>
	 */
	private HashMap<Long, SnomedQueryConcept> snomedConcepts = new HashMap<Long, SnomedQueryConcept>();

	/**
	 * <p>
	 * Base directory for data files when deserializing.
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
	 * @return The RawData sub-directory of baseDir.
	 */
	public String getRawDataDir() {
		return this.baseDir + "\\" + "RawData";
	}

	/**
	 * <p>
	 * Constructs a Path to the ParsedData directory.
	 * </p>
	 *
	 * @return The ParsedData sub-directory of baseDir.
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
	 *            Sets the baseDir value.
	 */
	public SnomedModelManager(String baseDirParam) {
		this.baseDir = baseDirParam;
	}

	/**
	 * <p>
	 * Loads the SNOMED Concepts into memory.
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
	 * Add SNOMED query concept to model manager.
	 * </p>
	 *
	 * @param concept
	 *            The concept that will be inserted into <b>snomedConcepts</b>.
	 * @throws Exception
	 *             General exception, custom message.
	 */
	public void add(SnomedQueryConcept concept) throws Exception {
		if (this.snomedConcepts.putIfAbsent(concept.getConceptId(),
				concept) != null)
			System.err.println(
					String.format("Error adding concept %s to dictionary.`",
							concept.getConceptId()));
	}

	SnomedQueryConcept deserializeConceptData() {
		long conceptId = this.binaryReader.getLong();
		String conceptFullyQualifiedName = this.getString();
		String[] conceptSynonyms = this.getStringArray();
		String conceptModule = this.getString();
		String conceptDefinitionStatus = this.getString();
		DateTime conceptEffectiveTime = this.getDateTime();

		SnomedQueryConcept concept = new SnomedQueryConcept(conceptId,
				conceptFullyQualifiedName, conceptSynonyms, conceptModule,
				conceptDefinitionStatus, conceptEffectiveTime);
		concept.setIsAParents(
				new SnomedQueryConcept[this.binaryReader.getInt()]);
		concept.setIsAChildren(
				new SnomedQueryConcept[this.binaryReader.getInt()]);
		return concept;
	}

	/**
	 * <p>
	 * Gets the length of the char[] to be deserialized first, then deserializes
	 * the bytes before validating them.
	 * </p>
	 *
	 * @return Parsed and validated string created from byte[].
	 */
	String getString() {
		String retVal;
		int length = this.binaryReader.getInt();
		char[] readChars = new char[length];
		for (int i = 0; i < length; i++) {
			readChars[i] = this.binaryReader.order(ByteOrder.LITTLE_ENDIAN)
					.getChar();
		}
		retVal = new String(readChars);
		return retVal;

	}

	/**
	 * <p>
	 * Constructs a String array by reading th length of the String array first
	 * out of <b>binaryReader</b> before.
	 * </p>
	 *
	 * @return Deserialized String array.
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
	 * @return A new DateTime object.
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
	 *            Concept to have relationships linked to it.
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
	 * Serialize concepts and relationships in memory to binary
	 * </p>
	 */
	public void serialize() {
		this.binaryWriter = new ByteArrayOutputStream();
		this.relationshipWriter = new ByteArrayOutputStream();
		try {
			FileOutputStream dataOutput = new FileOutputStream(
					new File(this.getParsedRecordsDir() + "\\"
							+ "SnomedQueryConcepts.Data.ser"));
			FileOutputStream relOutput = new FileOutputStream(
					new File(this.getParsedRecordsDir() + "\\"
							+ "SnomedQueryConcepts.IsARelationships.ser"));
			this.binaryWriter = this.write(this.binaryWriter,
					this.snomedConcepts.size());
			for (SnomedQueryConcept concept : this.snomedConcepts.values()) {
				this.serializeConcept(concept);
			}

			dataOutput.write(this.binaryWriter.toByteArray());
			relOutput.write(this.relationshipWriter.toByteArray());

			dataOutput.flush();
			dataOutput.close();

			relOutput.flush();
			relOutput.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>
	 * Serializes the concept and relationships.
	 * </p>
	 * 
	 * @param concept
	 * @throws Exception
	 */
	void serializeConcept(SnomedQueryConcept concept) throws Exception {
		if (concept.getConceptId() == 45614000)
			System.out.println("");
		this.binaryWriter = this.write(this.binaryWriter,
				concept.getConceptId());
		this.binaryWriter = this.write(this.binaryWriter,
				concept.getConceptFullyQualifiedName());
		this.binaryWriter = this.write(this.binaryWriter,
				concept.getConceptSynonyms());
		this.binaryWriter = this.write(this.binaryWriter,
				concept.getConceptModule());
		this.binaryWriter = this.write(this.binaryWriter,
				concept.getConceptDefinitionStatus());
		this.binaryWriter = this.write(this.binaryWriter,
				concept.getConceptEffectiveTime().getMillis());

		this.binaryWriter = this.write(this.binaryWriter,
				concept.getIsAParents().length);
		this.binaryWriter = this.write(this.binaryWriter,
				concept.getIsAChildren().length);

		SnomedQueryConcept[] childArray = concept.getIsAChildren();
		for (int i = 0; i < childArray.length; i++) {
			SnomedQueryConcept child = childArray[i];
			this.relationshipWriter = this.write(this.relationshipWriter,
					child.getConceptId());
			int reverseIndex = -1;
			SnomedQueryConcept[] parentArray = child.getIsAParents();
			for (int j = 0; j < parentArray.length; j++) {
				if (parentArray[j].getConceptId() == concept.getConceptId()) {
					reverseIndex = j;
					break;
				}
			}

			if (reverseIndex == -1) {
				throw new Exception("Reverse snomed index not found");
			}
			this.relationshipWriter = this.write(this.relationshipWriter,
					reverseIndex);
		}
	}

	/**
	 * <p>
	 * Serializes an integer value.
	 * </p>
	 * 
	 * @param buffer
	 * @param value
	 * @return
	 * @throws IOException
	 */
	public ByteArrayOutputStream write(ByteArrayOutputStream buffer, int value)
			throws IOException {
		buffer.write(ByteBuffer.allocate(Integer.BYTES)
				.order(ByteOrder.LITTLE_ENDIAN).putInt(value).array());
		return buffer;
	}

	/**
	 * <p>
	 * Serializes a long value.
	 * </p>
	 * 
	 * @param buffer
	 * @param value
	 * @return
	 * @throws IOException
	 */
	public ByteArrayOutputStream write(ByteArrayOutputStream buffer, long value)
			throws IOException {
		buffer.write(ByteBuffer.allocate(Long.BYTES)
				.order(ByteOrder.LITTLE_ENDIAN).putLong(value).array());
		return buffer;
	}

	/**
	 * <p>
	 * Serializes a long value.
	 * </p>
	 * 
	 * @param buffer
	 * @param value
	 * @return
	 * @throws IOException
	 */
	public ByteArrayOutputStream write(ByteArrayOutputStream buffer,
			DateTime value) throws IOException {
		buffer.write(
				ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN)
						.putLong(value.getMillis()).array());
		return buffer;
	}

	/**
	 * <p>
	 * Serializes a String value.
	 * </p>
	 * 
	 * @param buffer
	 * @param value
	 * @return
	 * @throws IOException
	 */
	public ByteArrayOutputStream write(ByteArrayOutputStream buffer,
			String value) throws IOException {
		buffer.write(ByteBuffer.allocate(Integer.BYTES)
				.order(ByteOrder.LITTLE_ENDIAN).putInt(value.length()).array());
		for (int i = 0; i < value.length(); i++) {
			buffer.write(ByteBuffer.allocate(Character.BYTES)
					.order(ByteOrder.LITTLE_ENDIAN)
					.putChar(value.toCharArray()[i]).array());
		}
		return buffer;
	}

	/**
	 * <p>
	 * Serializes a long array value.
	 * </p>
	 * 
	 * @param buffer
	 * @param value
	 * @return
	 * @throws IOException
	 */
	public ByteArrayOutputStream write(ByteArrayOutputStream buffer,
			long[] value) throws IOException {
		buffer.write(ByteBuffer.allocate(Integer.BYTES)
				.order(ByteOrder.LITTLE_ENDIAN).putInt(value.length).array());
		for (long l : value) {
			buffer.write(ByteBuffer.allocate(Long.BYTES)
					.order(ByteOrder.LITTLE_ENDIAN).putLong(l).array());
		}
		return buffer;
	}

	/**
	 * <p>
	 * Serializes a string array value.
	 * </p>
	 * 
	 * @param buffer
	 * @param value
	 * @return
	 * @throws IOException
	 */
	public ByteArrayOutputStream write(ByteArrayOutputStream buffer,
			String[] value) throws IOException {
		buffer.write(ByteBuffer.allocate(Integer.BYTES)
				.order(ByteOrder.LITTLE_ENDIAN).putInt(value.length).array());
		for (int i = 0; i < value.length; i++) {
			buffer = this.write(buffer, value[i]);
		}
		return buffer;
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
	 *            Persisted list of items that have already been viewed.
	 * @param parent
	 *            SnomedQueryConcept of which children is about to be parsed.
	 * @param childId
	 *            Target child Id that is being searched.
	 * @return True if there is a child object with child id.
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
	 * Return true if parent is an ancestor of child.
	 * </p>
	 *
	 * @param parentId
	 *            Parent Id.
	 * @param childId
	 *            Child Id.
	 * @return True if there is a child ancestor.
	 */
	public boolean isChild(long parentId, long childId) {
		SnomedQueryConcept parent = this.getConceptById(parentId);
		HashMap<Long, SnomedQueryConcept> visitedItems = new HashMap<>();
		return this.isChild(visitedItems, parent, childId);
	}

	/**
	 * Collect children of parent and continue visitor pattern onward with
	 * children.
	 *
	 * @param visitedItems
	 *            Items already visited.
	 * @param parent
	 *            Parent concept.
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
	 * children.
	 * </p>
	 *
	 * @param conceptId
	 *            the queried Id.
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
	 * @param visitedItems
	 *            hashMap of gathered items.
	 * @param child
	 *            current child looking for parents.
	 * @return the collected HashMap of objects.
	 */
	public HashMap<Long, SnomedQueryConcept> findAncestors(
			HashMap<Long, SnomedQueryConcept> visitedItems,
			SnomedQueryConcept child) {
		for (SnomedQueryConcept parent : child.getIsAParents()) {
			if (!visitedItems.containsKey(parent.getConceptId())) {
				visitedItems.put(parent.getConceptId(), parent);
				this.findAncestors(visitedItems, parent);
			}
		}
		return visitedItems;
	}

	/**
	 * <p>
	 * The start of finding all the parent Id's from target Id.
	 * </p>
	 *
	 * @param conceptId
	 *            the targetId that is to be queried.
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

	/**
	 * <p>
	 * Outputs a Tab delimeted closure table file to the directory of choice.
	 * </p>
	 * 
	 * @param ouputFile
	 */
	public void createClosureTable(String ouputFile) {
		List<Long[]> outputSet = new ArrayList<>();
		Long currentSource;
		Long currentDestination;
		File outputFile = new File(ouputFile);
		try {
			FileWriter fWriter = new FileWriter(outputFile);
			PrintWriter pWriter = new PrintWriter(fWriter);
			for (SnomedQueryConcept conceptSource : this.snomedConcepts
					.values()) {
				currentSource = conceptSource.getConceptId();
				SnomedQueryConcept[] conceptArray = this
						.findDescendants(conceptSource.getConceptId());
				for (SnomedQueryConcept conceptDestination : conceptArray) {
					currentDestination = conceptDestination.getConceptId();
					outputSet
							.add(new Long[]{currentSource, currentDestination});
				}
			}
			for (Long[] pair : outputSet) {
				pWriter.write(String.format("%s\t%s\n", pair[0], pair[1]));
			}
			pWriter.close();

			fWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
