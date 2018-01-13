import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Describes a file's metadata: URL, file name, size, and which parts already
 * downloaded to disk.
 *
 * The metadata (or at least which parts already downloaded to disk) is
 * constantly stored safely in disk. When constructing a new metadata object, we
 * first check the disk to load existing metadata.
 *
 * CHALLENGE: try to avoid metadata disk footprint of O(n) in the average case
 * HINT: avoid the obvious bitmap solution, and think about ranges...
 */
class DownloadableMetadata implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private final String metadataFilename;
	private String filename;
	private String url;
	private Range[] ranges;
	private int lastRangeThatWritten = 0;
	private long sizeOfFile;
	private File metadataFile;
	//private transient FileOutputStream fileOutStream;

	DownloadableMetadata(String url, Range[] ranges, long fileSize) throws FileNotFoundException {
		this.url = url;
		this.filename = getName(url);
		this.metadataFilename = getMetadataName(filename);
		this.ranges = ranges;
		this.sizeOfFile = fileSize;
		this.metadataFile = new File(this.metadataFilename);
		//this.fileOutStream = new FileOutputStream(this.metadataFile);
	}

	private static String getMetadataName(String filename) {
		return filename + ".metadata";
	}

	private static String getName(String path) {
		return path.substring(path.lastIndexOf('/') + 1, path.length());
	}

	/**
	 * add the sent range to array, remember which ranges was already sent.
	 * 
	 * @param range
	 *            - range to add
	 * @param place
	 *            - place in array
	 */
	void addRange(Range range, int place) {
		// TODO
	}

	String getFilename() {
		return filename;
	}

	/**
	 * check if all the ranges is in the array, i.e. all ranges passed, if so
	 * return true.
	 * 
	 * @return
	 */
	// boolean isCompleted() {
	// // TODO
	// }

	
	void delete() throws IOException {
		//fileOutStream.close();
		metadataFile.delete();
	}

	
	Range getMissingRange() {
		for (int i = lastRangeThatWritten; i < ranges.length; i++) {
			if (!ranges[i].getWasWritten()) {
				lastRangeThatWritten = i + 1;
				return ranges[i];
			}
		}
		return null;
	}

	String getUrl() {
		return url;
	}

	long getSizeOfFile() {
		return this.sizeOfFile;
	}

	File getFile() {
		return this.metadataFile;
	}
}
